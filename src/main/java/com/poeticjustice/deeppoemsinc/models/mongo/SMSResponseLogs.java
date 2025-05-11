package com.poeticjustice.deeppoemsinc.models.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "SMSResponseLogs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SMSResponseLogs {

    @Id
    private String id;
    private String reference;
    private String message;
    private String recipient;
    private String gateway;
    private boolean isSuccessful;
    private String response;
    private String countryCode;
    private String messageId;
    private String cost;
}