package com.poeticjustice.deeppoemsinc.service.domain;

import com.poeticjustice.deeppoemsinc.application.dtos.UploadEventPayload;
import com.poeticjustice.deeppoemsinc.application.events.publisher.UploadEventProducer;
import com.poeticjustice.deeppoemsinc.domain.models.mongo.FileMeta;
import com.poeticjustice.deeppoemsinc.domain.Repository.mongodb.FileMetaRepository;
import com.poeticjustice.deeppoemsinc.domain.service.FileStorageService;
import io.minio.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Testcontainers
@DataMongoTest
@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private UploadEventProducer eventPublisher;

    @Autowired
    private FileMetaRepository fileMetaRepository;

    private FileStorageService service;

    private final String PUBLIC_BUCKET = "publicuploader";
    private final String PRIVATE_BUCKET = "privateuploader";
    private final String MINIO_ENDPOINT = "minio.example.com";

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4")
            .withExposedPorts(27017);

    @BeforeAll
    static void setUp() {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setup() {
        fileMetaRepository.deleteAll(); // Clear MongoDB before each test
        service = new FileStorageService(minioClient, fileMetaRepository, eventPublisher,
                PUBLIC_BUCKET, PRIVATE_BUCKET, MINIO_ENDPOINT);
    }

    @Test
    void storeFile_simpleVariant_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hi".getBytes());
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String url = service.storeFile(file, "Docs", "user123");

        assertNotNull(url);
        assertTrue(url.contains("fileuploader"));
        verify(minioClient, atLeastOnce()).putObject(any(PutObjectArgs.class));
        verify(eventPublisher, times(1)).publishUploadEvent(any(UploadEventPayload.class));

        List<FileMeta> metas = fileMetaRepository.findByUserId("user123");
        assertEquals(1, metas.size());
        assertEquals("hello.txt", metas.get(0).getFileName());
        assertEquals("Docs", metas.get(0).getCategory());
    }

    @Test
    void storeFile_genericVariant_publicBucket_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hi".getBytes());
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String url = service.storeFile(file, "Docs", "user123", true, 1);

        assertNotNull(url);
        assertTrue(url.contains(PUBLIC_BUCKET));
        assertTrue(url.contains(MINIO_ENDPOINT));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
        verify(eventPublisher, times(1)).publishUploadEvent(any(UploadEventPayload.class));

        List<FileMeta> metas = fileMetaRepository.findByUserId("user123");
        assertEquals(1, metas.size());
        assertEquals("hello.txt", metas.get(0).getFileName());
        assertEquals(PUBLIC_BUCKET, metas.get(0).getBucketName());
    }

    @Test
    void storeFile_genericVariant_privateBucket_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hi".getBytes());
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio.example.com/presigned-url");

        String url = service.storeFile(file, "Docs", "user123", false, 1);

        assertNotNull(url);
        assertTrue(url.contains("presigned-url"));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
        verify(minioClient, times(1)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
        verify(eventPublisher, times(1)).publishUploadEvent(any(UploadEventPayload.class));

        List<FileMeta> metas = fileMetaRepository.findByUserId("user123");
        assertEquals(1, metas.size());
        assertEquals("hello.txt", metas.get(0).getFileName());
        assertEquals(PRIVATE_BUCKET, metas.get(0).getBucketName());
        assertNotNull(metas.get(0).getExpiryDate());
    }

    @Test
    void regeneratePresignedUrl_fileNotFound_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.regeneratePresignedUrl("nope", "notfound.png", 1));
    }

    @Test
    void regeneratePresignedUrl_success() throws Exception {
        FileMeta meta = new FileMeta();
        meta.setUserId("user123");
        meta.setFileName("hello.txt");
        meta.setBucketName(PRIVATE_BUCKET);
        meta.setObjectKey("user123/hello.txt");
        fileMetaRepository.save(meta);

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio.example.com/presigned-url");

        String url = service.regeneratePresignedUrl("user123", "hello.txt", 1);

        assertEquals("https://minio.example.com/presigned-url", url);
        verify(minioClient, times(1)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void deleteExpiredFiles_deletesAndRemovesDBEntry() throws Exception {
        FileMeta expired = new FileMeta();
        expired.setUserId("user123");
        expired.setFileName("old.png");
        expired.setBucketName(PRIVATE_BUCKET);
        expired.setObjectKey("user123/old.png");
        expired.setExpiryDate(LocalDateTime.now().minusHours(1));
        fileMetaRepository.save(expired);

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        service.deleteExpiredFiles();

        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
        assertEquals(0, fileMetaRepository.findByUserId("user123").size());
    }

    @Test
    void deleteExpiredFiles_noExpiredFiles_doesNothing() throws Exception {
        FileMeta notExpired = new FileMeta();
        notExpired.setUserId("user123");
        notExpired.setFileName("fresh.png");
        notExpired.setBucketName(PRIVATE_BUCKET);
        notExpired.setObjectKey("user123/fresh.png");
        notExpired.setExpiryDate(LocalDateTime.now().plusHours(1));
        fileMetaRepository.save(notExpired);

        service.deleteExpiredFiles();

        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
        assertEquals(1, fileMetaRepository.findByUserId("user123").size());
    }

    @Test
    void listUserFiles_publicBucket_returnsPermanentUrls() throws Exception {
        FileMeta meta = new FileMeta();
        meta.setUserId("user123");
        meta.setFileName("file.txt");
        meta.setBucketName(PUBLIC_BUCKET);
        meta.setObjectKey("user123/file.txt");
        fileMetaRepository.save(meta);

        List<String> urls = service.listUserFiles("user123", true, 1);

        assertEquals(1, urls.size());
        assertTrue(urls.get(0).contains(PUBLIC_BUCKET));
        assertTrue(urls.get(0).contains(MINIO_ENDPOINT));
        verify(minioClient, never()).getPresignedObjectUrl(any());
    }

    @Test
    void listUserFiles_privateBucket_returnsPresignedUrls() throws Exception {
        FileMeta meta = new FileMeta();
        meta.setUserId("user123");
        meta.setFileName("file.txt");
        meta.setBucketName(PRIVATE_BUCKET);
        meta.setObjectKey("user123/file.txt");
        fileMetaRepository.save(meta);

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio.example.com/presigned-url");

        List<String> urls = service.listUserFiles("user123", false, 1);

        assertTrue(urls.get(0).contains("presigned-url"));
        verify(minioClient, times(1)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @AfterAll
    static void tearDown() {
        if (mongoDBContainer != null && mongoDBContainer.isRunning()) {
            mongoDBContainer.stop();
        }
    }
}