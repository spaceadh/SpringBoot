package com.poeticjustice.deeppoemsinc.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {

    @NotBlank
    private String reference;

    @NotBlank
    private String message;

    @Size(max = 255)
    private String subject;

    private List<Recipient> recipients;
    private Map<String, String> tokens;
    private boolean hasAttachment;
    private List<Attachment> attachment;
    private String productName;
    private String language;
    private String pushDetails;
    private String countryCode;

     public Boolean getHasAttachment() {
        return hasAttachment;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Recipient {
        private String to;
        private int platform;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attachment {
        private String fileName;
        private String fileType;
        private String base64Attachment;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Recipient> recipients) {
        this.recipients = recipients;
    }
}
