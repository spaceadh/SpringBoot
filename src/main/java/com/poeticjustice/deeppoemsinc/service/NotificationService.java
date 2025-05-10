package com.poeticjustice.deeppoemsinc.service;

import com.poeticjustice.deeppoemsinc.dtos.NotificationRequest;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidNotificationException;
import com.poeticjustice.deeppoemsinc.models.mongo.NotificationDocument;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.NotificationMongoRepository;
import com.poeticjustice.deeppoemsinc.helper.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationMongoRepository notificationRepository;
    private final ValidationHelper validationHelper;
    private final Map<String, Map<String, String>> errorCodes;

    public NotificationService(NotificationMongoRepository notificationRepository,
                              ValidationHelper validationHelper,
                              Map<String, Map<String, String>> errorCodes) {
        this.notificationRepository = notificationRepository;
        this.validationHelper = validationHelper;
        this.errorCodes = errorCodes;
    }

    public NotificationDocument saveNotification(NotificationRequest request, Locale locale) {
        // Validate the request
        ValidationHelper.ValidationResult validationResult = validationHelper.validateNotificationData(request);
        if (!validationResult.isValid()) {
            String errorCode = validationResult.getErrorCode();
            String errorMessage = getLocalizedErrorMessage(errorCode, locale);
            logger.error("Validation failed: {} - {}", errorCode, errorMessage);
            throw new InvalidNotificationException(errorCode, errorMessage);
        }

        // Check for duplicate reference
        if (notificationRepository.existsByReference(request.getReference())) {
            String errorCode = "410";
            String errorMessage = getLocalizedErrorMessage(errorCode, locale);
            logger.error("Duplicate reference: {} - {}", errorCode, errorMessage);
            throw new InvalidNotificationException(errorCode, errorMessage);
        }

        // Sanitize tokens
        Map<String, String> sanitizedTokens = validationHelper.sanitizeTokens(request.getTokens());

         // Map DTO to document
        NotificationDocument document = new NotificationDocument();
        document.setId(UUID.randomUUID().toString());
        document.setReference(request.getReference());
        List<NotificationDocument.Recipient> documentRecipients = request.getRecipients().stream()
                .map(reqRecipient -> {
                    NotificationDocument.Recipient docRecipient = new NotificationDocument.Recipient();
                    docRecipient.setTo(reqRecipient.getTo());
                    docRecipient.setPlatform(reqRecipient.getPlatform());
                    return docRecipient;
                })
                .toList();
        document.setRecipients(documentRecipients);
        document.setMessage(request.getMessage());
        document.setSubject(request.getSubject());
        document.setTokens(sanitizedTokens);
        document.setHasAttachment(request.getHasAttachment());
        // Map NotificationRequest.Attachment to NotificationDocument.Attachment
        List<NotificationDocument.Attachment> documentAttachments = request.getAttachment().stream()
                .map(reqAttachment -> {
                    NotificationDocument.Attachment docAttachment = new NotificationDocument.Attachment();
                    docAttachment.setFileName(reqAttachment.getFileName());
                    docAttachment.setFileType(reqAttachment.getFileType());
                    docAttachment.setBase64Attachment(reqAttachment.getBase64Attachment());
                    return docAttachment;
                })
                .toList();
        document.setAttachment(documentAttachments);
        document.setProductName(request.getProductName());
        document.setLanguage(request.getLanguage());
        document.setPushDetails(request.getPushDetails());
        document.setCountryCode(request.getCountryCode());

        // Save to MongoDB
        try {
            return notificationRepository.save(document);
        } catch (Exception e) {
            logger.error("Failed to save notification: {}", e.getMessage());
            throw new RuntimeException("Failed to save notification", e);
        }
    }

    public String getLocalizedErrorMessage(String errorCode, Locale locale) {
        Map<String, String> messages = errorCodes.getOrDefault(errorCode, new HashMap<>());
        String language = locale.getLanguage().toLowerCase();
        return messages.getOrDefault(language, messages.getOrDefault("en", "Unknown error: " + errorCode));
    }
}