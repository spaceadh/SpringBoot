package com.poeticjustice.deeppoemsinc.application.events.consumer;

import com.poeticjustice.deeppoemsinc.application.dtos.UploadEventPayload;
import com.poeticjustice.deeppoemsinc.domain.Repository.mongodb.FileMetaRepository;
import com.poeticjustice.deeppoemsinc.domain.models.mongo.FileMeta;
import com.poeticjustice.deeppoemsinc.domain.service.FileStorageService;
import com.poeticjustice.deeppoemsinc.domain.service.QuotaService;
import com.poeticjustice.deeppoemsinc.infrastructure.config.rabbit.RabbitMQConfig;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class UploadEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UploadEventConsumer.class);

    private final FileMetaRepository fileMetaRepository;
    private final QuotaService quotaService;
    private final FileStorageService fileStorageService;

    public UploadEventConsumer(FileMetaRepository fileMetaRepository, QuotaService quotaService, FileStorageService fileStorageService) {
        this.fileMetaRepository = fileMetaRepository;
        this.quotaService = quotaService;
        this.fileStorageService = fileStorageService;
    }

    @RabbitListener(queues = RabbitMQConfig.FILE_UPLOAD_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleUploadEvent(UploadEventPayload payload) {
        logger.info("Processing upload event: {}", payload);

        String userId = payload.getUploaderId();
        String fileName = payload.getFileName();
        String objectKey = payload.getObjectKey();

        long fileSize = 0;
        try {
            // Extract fileSize from metadata map (sent in notify-upload)
            fileSize = Long.parseLong(payload.getMetadata().getOrDefault("fileSize", "0"));
        } catch (NumberFormatException e) {
            logger.warn("Invalid file size metadata for upload event: {}", payload);
        }

        // Check and update quota atomically
        if (!quotaService.hasEnoughQuota(userId, fileSize, payload.getMetadata().get("client"))) {
            logger.warn("Quota exceeded for user: {}", userId);
            // Optionally publish a quota exceeded event or notify user
            return;
        }

        quotaService.updateQuota(userId, fileSize, payload.getMetadata().get("client"));

        // Save file metadata in DB
        FileMeta meta = new FileMeta();
        meta.setUserId(userId);
        meta.setFileName(fileName);
        meta.setCategory(payload.getMetadata().get("category"));
        meta.setSizeInBytes(fileSize);
        meta.setBucketName(fileStorageService.getPrivateBucket());
        meta.setObjectKey(objectKey);
        meta.setUploadedAt(LocalDateTime.now());

        fileMetaRepository.save(meta);

        // Mark event as completed if needed or publish further events

        logger.info("Upload event processed successfully for user: {}, file: {}", userId, fileName);
    }

    @RabbitListener(queues = RabbitMQConfig.FILE_PROCESS_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleProcessEvent(UploadEventPayload payload) {
        logger.info("Processing file process event from {}: {}", RabbitMQConfig.FILE_PROCESS_QUEUE, payload);

        String userId = payload.getUploaderId();
        String fileName = payload.getFileName();

        // Example processing logic (e.g., generate thumbnail, analyze file)
        logger.info("Processing file: {} for user: {} in queue: {}", fileName, userId, RabbitMQConfig.FILE_PROCESS_QUEUE);

        // Update metadata or perform additional actions
        FileMeta meta = fileMetaRepository.findByUserIdAndFileName(userId, fileName);
        if (meta != null) {
            meta.setProcessed(true); // Hypothetical field
            fileMetaRepository.save(meta);
        }

        logger.info("File process event completed for user: {}, file: {} in queue: {}", userId, fileName, RabbitMQConfig.FILE_PROCESS_QUEUE);
    }

}
