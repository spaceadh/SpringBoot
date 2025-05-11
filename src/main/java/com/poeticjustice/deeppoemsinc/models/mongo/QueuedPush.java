package com.poeticjustice.deeppoemsinc.models.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "QueuedPush")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueuedPush {
    @Id
    private String id;
    private String notificationId;
    private String reference;
    private String recipient;
    private String message;
    private Map<String, String> tokens;
    private String pushDetails;
    private String productName;
    private String language;
    private String countryCode;

    @Builder.Default
    private boolean isSent = false;
}