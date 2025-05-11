package com.poeticjustice.deeppoemsinc.service;

import com.poeticjustice.deeppoemsinc.dtos.NotificationRequest;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidNotificationException;
import com.poeticjustice.deeppoemsinc.models.mongo.NotificationDocument;

import jakarta.annotation.PostConstruct;

import com.poeticjustice.deeppoemsinc.Repository.mongodb.NotificationMongoRepository;
import com.poeticjustice.deeppoemsinc.helpers.ValidationHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final ObjectMapper objectMapper;

    public NotificationService(
            NotificationMongoRepository notificationRepository,
            ValidationHelper validationHelper,
            @Qualifier("errorCodesMap") Map<String, Map<String, String>> errorCodes,
            ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.validationHelper = validationHelper;
        this.errorCodes = errorCodes;
        this.objectMapper = objectMapper;
        logger.info("NotificationService initialized with {} error codes", errorCodes.size());
    }

    @PostConstruct
    public void init() {
        logger.info("PostConstruct: errorCodes map contains {} entries", errorCodes.size());
        if (!errorCodes.isEmpty()) {
            logger.debug("Sample error code entry: {}", errorCodes.get("410"));
        }
    }
    
    public NotificationDocument saveNotification(NotificationRequest request, Locale locale) {
        // Log the request for debugging
        try {
            logger.info("Received NotificationRequest: {}", objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            logger.error("Failed to log NotificationRequest: {}", e.getMessage());
        }

        // Validate the request
        ValidationHelper.ValidationResult validationResult = validationHelper.validateNotificationData(request);
        if (!validationResult.isValid()) {
            String errorCode = validationResult.getErrorCode();
            String errorMessage = getLocalizedErrorMessage(errorCode, locale);
            logger.error("Validation failed: {} - {}", errorCode, errorMessage);
            throw new InvalidNotificationException(errorCode, errorMessage);
        }

        // Check for duplicate reference
        boolean exists;
        try {
            exists = notificationRepository.existsByReference(request.getReference());
        } catch (Exception e) {
            logger.error("Error checking notification existence: {}", e.getMessage());
            exists = false;
        }

        if (exists) {
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
        List<NotificationDocument.Attachment> documentAttachments = request.getAttachment() != null
                ? request.getAttachment().stream()
                    .map(reqAttachment -> {
                        NotificationDocument.Attachment docAttachment = new NotificationDocument.Attachment();
                        docAttachment.setFileName(reqAttachment.getFileName());
                        docAttachment.setFileType(reqAttachment.getFileType());
                        docAttachment.setBase64Attachment(reqAttachment.getBase64Attachment());
                        return docAttachment;
                    })
                    .toList()
                : List.of();
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
        logger.info("Fetching localized error message for code: {} in locale: {}", errorCode, locale);
        logger.debug("Current errorCodes map size: {}", errorCodes.size());
        Map<String, String> messages = errorCodes.getOrDefault(errorCode, new HashMap<>());
        String language = locale.getLanguage().toLowerCase();
        String defaultMessage = "Unknown error: " + errorCode;
        String errorMessage = messages.getOrDefault(language, messages.getOrDefault("en", defaultMessage));

        if (messages.isEmpty()) {
            logger.warn("No error message found for code: {} in any language. Using default: {}", errorCode, defaultMessage);
        } else if (!messages.containsKey(language) && !messages.containsKey("en")) {
            logger.warn("No error message found for code: {} in language: {} or default 'en'. Using default: {}", 
                        errorCode, language, defaultMessage);
        } else {
            logger.info("Retrieved error message for code: {} in language: {} - {}", errorCode, language, errorMessage);
        }
        return errorMessage;
    }
}