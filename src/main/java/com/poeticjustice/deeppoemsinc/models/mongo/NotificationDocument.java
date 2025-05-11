package com.poeticjustice.deeppoemsinc.models.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Notifications")
public class NotificationDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String reference;

    private List<Recipient> recipients;
    private String message;
    private String subject;
    private Map<String, String> tokens;
    private Boolean hasAttachment;
    private List<Attachment> attachment;
    private String productName;
    private String language;
    private Object pushDetails;
    private String countryCode;
    
    @Builder.Default
    private boolean isQueued = false;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Recipient {
        private String  to;
        private Integer platform;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Attachment {
        private String fileName;
        private String fileType;
        private String base64Attachment;
    }

    public boolean hasAttachment() {
        return hasAttachment;
    }
    public void setHasAttachment(boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public boolean IsQueued() {
        return isQueued;
    }
    public void setIsQueued(boolean isQueued) {
        this.isQueued = isQueued;
    }
    

    // public List<Recipient> getRecipients() {
    //     return recipients;
    // }

    // public void setRecipients(List<Recipient> recipients) {
    //     this.recipients = recipients;
    // }
}