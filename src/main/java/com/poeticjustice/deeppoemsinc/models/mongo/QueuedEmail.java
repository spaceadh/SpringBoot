package com.poeticjustice.deeppoemsinc.models.mongo;

import com.poeticjustice.deeppoemsinc.dtos.NotificationRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "QueuedEmails")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueuedEmail {

    @Id
    private String id;
    private String notificationId;
    private String reference;
    private String recipient;
    private String message;
    private String subject;
    private Map<String, String> tokens;

    @Builder.Default
    private boolean hasAttachment = false;
    private List<NotificationDocument.Attachment> attachment;
    private String productName;
    private String language;
    private String countryCode;

    public boolean hasAttachment() {
        return hasAttachment;
    }
    public void setHasAttachment(boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }
}