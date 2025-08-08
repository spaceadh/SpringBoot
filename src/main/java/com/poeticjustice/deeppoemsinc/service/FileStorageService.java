package com.poeticjustice.deeppoemsinc.service;


import com.poeticjustice.deeppoemsinc.events.publisher.UploadEventPublisher;
import com.poeticjustice.deeppoemsinc.models.mysql.FileMeta;
import com.poeticjustice.deeppoemsinc.Repository.mysql.FileMetaRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Service
public class FileStorageService {
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private FileMetaRepository fileMetaRepository;
    @Autowired
    private UploadEventPublisher eventPublisher;

    public String storeFile(MultipartFile file, String category, String userId) throws Exception {
        String bucketName = "fileuploader";
        String objectName = userId + "/" + file.getOriginalFilename();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());

        FileMeta fileMeta = new FileMeta();
        fileMeta.setFileName(file.getOriginalFilename());
        fileMeta.setCategory(category);
        fileMeta.setSizeInBytes(file.getSize());
        fileMeta.setUserId(userId);
        fileMeta.setStorageUrl(bucketName + "/" + objectName);
        fileMeta.setUploadedAt(LocalDateTime.now());
        fileMetaRepository.save(fileMeta);

        eventPublisher.publishUploadEvent(userId, file.getOriginalFilename(), category);
        return fileMeta.getStorageUrl();
    }
}