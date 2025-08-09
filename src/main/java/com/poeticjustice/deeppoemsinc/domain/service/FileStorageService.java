package com.poeticjustice.deeppoemsinc.domain.service;

import com.poeticjustice.deeppoemsinc.domain.models.mongo.*;
import com.poeticjustice.deeppoemsinc.domain.Repository.mongodb.*;
import com.poeticjustice.deeppoemsinc.application.dtos.UploadEventPayload;
import com.poeticjustice.deeppoemsinc.application.events.publisher.*;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileStorageService {
    private final MinioClient minioClient;
    private final FileMetaRepository fileMetaRepository;
    private final UploadEventProducer eventPublisher;

    private final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
     // buckets
    private final String publicBucket;
    private final String privateBucket;

    // Minio endpoint used for constructing permanent public URL (if behind gateway change accordingly)
    private final String minioEndpoint; // e.g. "minio.example.com"

    public FileStorageService(MinioClient minioClient,
                              FileMetaRepository fileMetaRepository,
                              UploadEventProducer eventPublisher,
                              @Value("${app.storage.public-bucket:publicuploader}") String publicBucket,
                              @Value("${app.storage.private-bucket:privateuploader}") String privateBucket,
                              @Value("${app.storage.endpoint:minio.example.com}") String minioEndpoint) {
        this.minioClient = minioClient;
        this.fileMetaRepository = fileMetaRepository;
        this.eventPublisher = eventPublisher;
        this.publicBucket = publicBucket;
        this.privateBucket = privateBucket;
        this.minioEndpoint = minioEndpoint;
    }

    public String storeFile(MultipartFile file, String category, String userId) throws Exception {
        String bucketName = "fileuploader";

        // ✅ Ensure bucket exists before uploading
        boolean isExist = minioClient.bucketExists(
            io.minio.BucketExistsArgs.builder()
                .bucket(bucketName)
                .build()
        );
        if (!isExist) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
        }

        String objectName = userId + "/" + file.getOriginalFilename();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .build()
        );

        FileMeta fileMeta = new FileMeta();
        fileMeta.setFileName(file.getOriginalFilename());
        fileMeta.setCategory(category);
        fileMeta.setSizeInBytes(file.getSize());
        fileMeta.setUserId(userId);
        fileMeta.setStorageUrl(bucketName + "/" + objectName);
        fileMeta.setUploadedAt(LocalDateTime.now());
        fileMetaRepository.save(fileMeta);

        UploadEventPayload payload = new UploadEventPayload();
        payload.setFileId(fileMeta.getId() != null ? fileMeta.getId().toString() : null);
        payload.setFileName(file.getOriginalFilename());
        payload.setFileType(file.getContentType());
        payload.setUploaderId(userId);
        payload.setUploadDate(System.currentTimeMillis());
        payload.setStatus(UploadEventPayload.UploadStatus.COMPLETED);
        payload.setMetadata(Map.of("category", category));
        eventPublisher.publishUploadEvent(payload);
        return fileMeta.getStorageUrl();
    }

    // Generic upload used by controller
    public String storeFileDynamicUtil(MultipartFile file, String category, String userId, boolean isPublic, int ttlHours) throws Exception {
        String bucket = isPublic ? publicBucket : privateBucket;
        String objectKey = userId + "/" + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());

        ensureBucketExists(bucket);

        // Put object
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());

        // Save metadata (store bucket + objectKey; do NOT store presigned URL permanently for private files)
        FileMeta meta = new FileMeta();
        meta.setUserId(userId);
        meta.setFileName(file.getOriginalFilename());
        meta.setCategory(category);
        meta.setSizeInBytes(file.getSize());
        meta.setBucketName(bucket);
        meta.setObjectKey(objectKey);
        meta.setUploadedAt(LocalDateTime.now());
        if (!isPublic) {
            meta.setExpiryDate(LocalDateTime.now().plusHours(ttlHours));
        }
        fileMetaRepository.save(meta);

        // Publish event
        UploadEventPayload payload = new UploadEventPayload();
        payload.setUploaderId(userId);
        payload.setFileName(file.getOriginalFilename());
        payload.setFileType(file.getContentType());
        payload.setUploadDate(System.currentTimeMillis());
        payload.setStatus(UploadEventPayload.UploadStatus.COMPLETED);
        payload.setMetadata(Map.of("category", category));
        eventPublisher.publishUploadEvent(payload);

        // Return URL depending on privacy
        if (isPublic) {
            return String.format("https://%s/%s/%s", minioEndpoint, bucket, objectKey);
        } else {
            // presigned URL valid for 1 hour (ttlHours) — we choose 3600 seconds or ttlHours*3600
            return generatePresignedUrl(bucket, objectKey, ttlHours * 3600);
        }
    }

    // Regenerate new presigned URL for private object
    public String regeneratePresignedUrl(String userId, String fileName, int ttlHours) throws Exception {
        List<FileMeta> metas = fileMetaRepository.findByUserId(userId);
        logger.info("Found {} files for user {}", metas.size(), userId);
        Optional<FileMeta> match = metas.stream()
                .filter(m -> fileName.equals(m.getFileName()) && privateBucket.equals(m.getBucketName()))
                .findFirst();
        if (match.isEmpty()) {
            throw new IllegalArgumentException("File not found for user");
        }
        FileMeta meta = match.get();
        return generatePresignedUrl(meta.getBucketName(), meta.getObjectKey(), ttlHours * 3600);
    }

    public String generatePresignedUrl(String bucket, String object, int expirySeconds) throws Exception {
        ensureBucketExists(bucket);
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(object)
                        .expiry(expirySeconds)
                        .build()
        );
    }

    // List files for a user and return full URLs (public permanent, private -> presigned)
    public List<String> listUserFiles(String userId, boolean isPublic, int ttlHours) throws Exception {
        String bucket = isPublic ? publicBucket : privateBucket;
        List<FileMeta> metas = fileMetaRepository.findByUserId(userId)
                .stream()
                .filter(m -> m.getBucketName().equals(bucket))
                .collect(Collectors.toList());

        List<String> urls = new ArrayList<>();
        for (FileMeta meta : metas) {
            if (isPublic) {
                urls.add(String.format("https://%s/%s/%s", minioEndpoint, bucket, meta.getObjectKey()));
            } else {
                urls.add(generatePresignedUrl(bucket, meta.getObjectKey(), ttlHours * 3600));
            }
        }
        return urls;
    }

    // Scheduled cleanup of expired private files — runs every 10 minutes
    @Scheduled(fixedDelayString = "${app.storage.cleanup-ms:600000}")
    public void deleteExpiredFiles() {
        try {
            List<FileMeta> expired = fileMetaRepository.findByExpiryDateBefore(LocalDateTime.now());
            for (FileMeta meta : expired) {
                try {
                    // delete from MinIO
                    minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(meta.getBucketName())
                            .object(meta.getObjectKey())
                            .build());
                } catch (Exception e) {
                    // log and continue
                    logger.error("Failed to remove object {}: {}", meta.getObjectKey(), e.getMessage());
                }
                logger.info("Deleted expired file: {} from bucket: {}", meta.getObjectKey(), meta.getBucketName());
                // remove DB entry
                fileMetaRepository.delete(meta);
            }
        } catch (Exception e) {
            System.err.println("Error during expired file cleanup: " + e.getMessage());
        }
    }

    // Ensure bucket exists
    private void ensureBucketExists(String bucket) throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            logger.info("Bucket {} does not exist, creating it", bucket);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    private String sanitize(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9\\._\\-]", "_");
    }

    public String getPrivateBucket() {
        return privateBucket;
    }
}