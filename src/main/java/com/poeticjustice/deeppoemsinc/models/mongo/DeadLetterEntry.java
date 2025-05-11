package com.poeticjustice.deeppoemsinc.models.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "DeadLetterQueue")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeadLetterEntry {

    @Id
    private String id;
    private String reference;
    private String recipient;
    private String message;
    private String countryCode;
    private String errorMessage;
    private long timestamp;
    private int retryAttempts;
}