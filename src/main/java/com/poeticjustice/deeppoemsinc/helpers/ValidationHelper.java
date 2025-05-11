package com.poeticjustice.deeppoemsinc.helpers;

import com.poeticjustice.deeppoemsinc.dtos.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class ValidationHelper {

    private static final Logger logger = LoggerFactory.getLogger(ValidationHelper.class);

    private static final Set<Integer> ALLOWED_PLATFORMS = Set.of(1, 2, 6);
    private static final Set<String> ALLOWED_PRODUCT_NAMES = new HashSet<>(Arrays.asList("Chamasoft", "Websacco", "EazzyChama"));
    private static final Set<String> ALLOWED_COUNTRY_CODES = new HashSet<>(Arrays.asList(
            "KE", "UG", "TZ", "RW", "SS", "BI", "SO", "ET", "SD", "CD", "NG", "ZA",
            "GH", "CI", "CM", "ML", "SN", "BF", "NE", "TG", "BJ", "GA", "CG", "AO",
            "US", "GB", "FR", "DE", "IT", "ES"
    ));

    public static final String INVALID_RECIPIENT_FORMAT = "413.1";
    public static final String INVALID_EMAIL_FORMAT = "413.2";
    public static final String INVALID_PHONE_FORMAT = "413.3";
    public static final String INVALID_KENYAN_PHONE = "413.4";

    public ValidationResult validateNotificationData(NotificationRequest notificationDto) {
        if (!isValidRequiredString(notificationDto.getReference())) {
            logger.warn("Invalid reference: {}", notificationDto.getReference());
            return new ValidationResult(false, "411");
        }
        if (!isValidRequiredString(notificationDto.getMessage())) {
            logger.warn("Invalid message: {}", notificationDto.getMessage());
            return new ValidationResult(false, "412");
        }
        ValidationResult recipientValidation = isValidRecipients(notificationDto.getRecipients(), notificationDto.getCountryCode());
        if (!recipientValidation.isValid()) {
            logger.warn("Invalid recipients: {}", notificationDto.getRecipients());
            return recipientValidation;
        }
        if (!isValidProductName(notificationDto.getProductName())) {
            logger.warn("Invalid product name: {}", notificationDto.getProductName());
            return new ValidationResult(false, "414");
        }
        if (!isValidCountryCode(notificationDto.getCountryCode())) {
            logger.warn("Invalid country code: {}", notificationDto.getCountryCode());
            return new ValidationResult(false, "415");
        }

        if (notificationDto.getHasAttachment()) {
            if (notificationDto.getAttachment() == null || notificationDto.getAttachment().isEmpty()) {
                logger.warn("No attachments when hasAttachment is true");
                return new ValidationResult(false, "416");
            }

            for (NotificationRequest.Attachment attachment : notificationDto.getAttachment()) {
                if (!isValidBase64(attachment.getBase64Attachment())) {
                    logger.warn("Invalid Base64 attachment: {}", attachment.getFileName());
                    return new ValidationResult(false, "419");
                }
                if (!isValidAttachmentSize(attachment.getBase64Attachment(), 5)) {
                    logger.warn("Attachment too large: {}", attachment.getFileName());
                    return new ValidationResult(false, "420");
                }
                if (!isAllowedMimeType(attachment.getFileType())) {
                    logger.warn("Invalid file type: {}", attachment.getFileType());
                    return new ValidationResult(false, "421");
                }
            }
        }

        return new ValidationResult(true, "");
    }

    public boolean isValidRequiredString(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public boolean isValidEmail(String email) {
        Pattern emailPattern = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
        return emailPattern.matcher(email).matches();
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        Pattern phonePattern = Pattern.compile("^\\d{10,15}$");
        return phonePattern.matcher(phoneNumber).matches();
    }

    public boolean isValidKenyanPhoneNumber(String phoneNumber) {
        Pattern kenyanPhonePattern = Pattern.compile("^(?:2547\\d{8}|2541\\d{8}|07\\d{8})$");
        return kenyanPhonePattern.matcher(phoneNumber).matches();
    }

    public boolean isValidProductName(String productName) {
        return isValidRequiredString(productName) && ALLOWED_PRODUCT_NAMES.contains(productName);
    }

    public boolean isValidCountryCode(String countryCode) {
        return isValidRequiredString(countryCode) && ALLOWED_COUNTRY_CODES.contains(countryCode);
    }

    public ValidationResult isValidRecipients(List<NotificationRequest.Recipient> recipients, String countryCode) {
        if (recipients == null || recipients.isEmpty()) {
            return new ValidationResult(false, INVALID_RECIPIENT_FORMAT);
        }

        for (NotificationRequest.Recipient recipient : recipients) {
            if (!isValidRequiredString(recipient.getTo()) || !ALLOWED_PLATFORMS.contains(recipient.getPlatform())) {
                return new ValidationResult(false, INVALID_RECIPIENT_FORMAT);
            }

            if (recipient.getPlatform() == 2 && !isValidEmail(recipient.getTo())) {
                return new ValidationResult(false, INVALID_EMAIL_FORMAT);
            }
            if (recipient.getPlatform() == 1 && !isValidPhoneNumber(recipient.getTo())) {
                return new ValidationResult(false, INVALID_PHONE_FORMAT);
            }
            if (recipient.getPlatform() == 1 && "KE".equals(countryCode) && !isValidKenyanPhoneNumber(recipient.getTo())) {
                return new ValidationResult(false, INVALID_KENYAN_PHONE);
            }
        }

        return new ValidationResult(true, "");
    }

    public boolean isValidFileName(String fileName) {
        if (!isValidRequiredString(fileName)) {
            return false;
        }
        String invalidChars = "[\\\\/:*?\"<>|]";
        return !Pattern.compile(invalidChars).matcher(fileName).find();
    }

    public boolean isValidBase64(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            logger.warn("Base64 string is null or empty");
            return false;
        }   
        try {
            logger.info("Base64 string length: {}", base64String.length());
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            return decodedBytes.length > 0;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid Base64 string: {}", e.getMessage());
            return false;
        }
    }

    public boolean isValidAttachmentSize(String base64String, int maxSizeMB) {
        if (!isValidRequiredString(base64String)) {
            return false;
        }
        try {
            byte[] data = Base64.getDecoder().decode(base64String);
            return data.length <= maxSizeMB * 1024 * 1024;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isValidAfricanPhoneNumber(String phoneNumber, String countryCode) {
        Map<String, String> patterns = new HashMap<>();
        patterns.put("KE", "^(?:254|0)?[17]\\d{8}$");
        patterns.put("NG", "^(?:234|0)?[789]\\d{9}$");
        patterns.put("ZA", "^(?:27|0)?[678]\\d{8}$");
        patterns.put("EG", "^(?:20|0)?1[0125]\\d{8}$");

        String pattern = patterns.getOrDefault(countryCode.toUpperCase(), "^\\d{10,15}$");
        return Pattern.compile(pattern).matcher(phoneNumber).matches();
    }

    public Map<String, String> sanitizeTokens(Map<String, String> tokens) {
        Map<String, String> sanitizedTokens = new HashMap<>();
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            String key = entry.getKey().replaceAll("[{}]", "");
            String value = isValidRequiredString(entry.getValue()) ? entry.getValue() : "N/A";
            sanitizedTokens.put(key, value);
        }
        return sanitizedTokens;
    }

    public boolean isAllowedMimeType(String fileTypeOrName) {
        Set<String> allowedTypes = new HashSet<>(Arrays.asList(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-powerpoint",
                "image/jpeg",
                "image/png",
                "image/gif",
                "image/svg+xml",
                "application/zip",
                "application/x-rar-compressed",
                "text/plain",
                "text/csv"
        ));

        if (allowedTypes.contains(fileTypeOrName)) {
            return true;
        }

        String mimeType = MimeTypes.getMimeType(fileTypeOrName);
        return allowedTypes.contains(mimeType);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorCode;

        public ValidationResult(boolean valid, String errorCode) {
            this.valid = valid;
            this.errorCode = errorCode;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    public static class MimeTypes {
        private static final Map<String, String> MIME_TYPE_MAP = new HashMap<>();

        static {
            MIME_TYPE_MAP.put(".pdf", "application/pdf");
            MIME_TYPE_MAP.put(".doc", "application/msword");
            MIME_TYPE_MAP.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            MIME_TYPE_MAP.put(".xls", "application/vnd.ms-excel");
            MIME_TYPE_MAP.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            MIME_TYPE_MAP.put(".ppt", "application/vnd.ms-powerpoint");
            MIME_TYPE_MAP.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
            MIME_TYPE_MAP.put(".txt", "text/plain");
            MIME_TYPE_MAP.put(".csv", "text/csv");
            MIME_TYPE_MAP.put(".jpg", "image/jpeg");
            MIME_TYPE_MAP.put(".jpeg", "image/jpeg");
            MIME_TYPE_MAP.put(".png", "image/png");
            MIME_TYPE_MAP.put(".gif", "image/gif");
            MIME_TYPE_MAP.put(".bmp", "image/bmp");
            MIME_TYPE_MAP.put(".svg", "image/svg+xml");
            MIME_TYPE_MAP.put(".zip", "application/zip");
            MIME_TYPE_MAP.put(".rar", "application/x-rar-compressed");
            MIME_TYPE_MAP.put(".bin", "application/octet-stream");
        }

        public static String getMimeType(String fileName) {
            if (!isValidRequiredString(fileName)) {
                return "application/octet-stream";
            }
            String extension = Optional.ofNullable(Path.of(fileName).getFileName())
                    .map(Path::toString)
                    .map(name -> {
                        int dotIndex = name.lastIndexOf('.');
                        return dotIndex >= 0 ? name.substring(dotIndex) : "";
                    })
                    .orElse("");
            return MIME_TYPE_MAP.getOrDefault(extension.toLowerCase(), "application/octet-stream");
        }

        private static boolean isValidRequiredString(String value) {
            return value != null && !value.trim().isEmpty();
        }
    }
}