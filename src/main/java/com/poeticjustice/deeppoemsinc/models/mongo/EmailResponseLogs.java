package com.poeticjustice.deeppoemsinc.models.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "EmailResponseLogs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailResponseLogs {
    @Id private String id;
    private String reference;
    private String recipient;
    private String gateway;
    private boolean isSuccessful;
    private String response;
    private String messageId;
}