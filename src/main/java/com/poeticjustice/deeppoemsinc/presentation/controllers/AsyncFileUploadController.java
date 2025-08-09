package com.poeticjustice.deeppoemsinc.presentation.controllers;

import com.poeticjustice.deeppoemsinc.application.events.publisher.UploadEventProducer;
import com.poeticjustice.deeppoemsinc.application.dtos.*;
import com.poeticjustice.deeppoemsinc.domain.service.FileStorageService;
import com.poeticjustice.deeppoemsinc.domain.service.QuotaService;

import com.poeticjustice.deeppoemsinc.domain.service.SubscriptionValidatorService;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/async-files")
public class AsyncFileUploadController {

    private final FileStorageService fileStorageService;
    private final QuotaService quotaService;
    private final SubscriptionValidatorService subscriptionValidator;
    private final UploadEventProducer eventProducer;

    public AsyncFileUploadController(FileStorageService fileStorageService,
                                     QuotaService quotaService,
                                     SubscriptionValidatorService subscriptionValidator,
                                     UploadEventProducer eventProducer) {
        this.fileStorageService = fileStorageService;
        this.quotaService = quotaService;
        this.subscriptionValidator = subscriptionValidator;
        this.eventProducer = eventProducer;
    }

    /**
     * Step 1: Request a presigned URL for direct upload
     */
    @PostMapping("/presigned-url")
    public ResponseEntity<?> getPresignedUrl(@RequestBody @Valid PresignedUrlRequestDto request) {
        try {
            if (!subscriptionValidator.isSubscribed(request.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "User not subscribed"));
            }

            if (!quotaService.hasEnoughQuota(request.getUserId(), request.getFileSize(), request.getClient())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Quota exceeded"));
            }

            // Generate a presigned URL (private bucket, TTL 1 hour)
            String objectKey = request.getUserId() + "/" + UUID.randomUUID() + "-" + sanitize(request.getFileName());
            String presignedUrl = fileStorageService.generatePresignedUrl(
                    fileStorageService.getPrivateBucket(), objectKey, 3600);

            // Return presigned URL and objectKey so client can notify later
            return ResponseEntity.ok(Map.of(
                    "presignedUrl", presignedUrl,
                    "objectKey", objectKey
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Step 2: Client calls this after uploading directly to MinIO
     */
    @PostMapping("/notify-upload")
    public ResponseEntity<?> notifyUpload(@RequestBody @Valid UploadNotifyRequestDto request) {
        try {
            // Validate subscription & quota again (optional, but safer)
            if (!subscriptionValidator.isSubscribed(request.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "User not subscribed"));
            }

            if (!quotaService.hasEnoughQuota(request.getUserId(), request.getFileSize(), request.getClient())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Quota exceeded"));
            }

            // Publish event to queue for async processing
            UploadEventPayload payload = new UploadEventPayload();
            payload.setUploaderId(request.getUserId());
            payload.setFileName(request.getFileName());
            payload.setFileType(null); // optional: client can send MIME type if available
            payload.setUploadDate(System.currentTimeMillis());
            payload.setStatus(UploadEventPayload.UploadStatus.PENDING);
            payload.setMetadata(Map.of("category", request.getCategory(),
                                       "fileSize", String.valueOf(request.getFileSize()),
                                       "client", request.getClient()));
            payload.setObjectKey(request.getUserId() + "/" + sanitize(request.getFileName()));

            eventProducer.publishUploadEvent(payload);

            return ResponseEntity.ok(Map.of("message", "Upload event queued for processing"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    private String sanitize(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9\\._\\-]", "_");
    }
}