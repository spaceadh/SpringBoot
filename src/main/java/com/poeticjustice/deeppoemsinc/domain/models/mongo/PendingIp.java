package com.poeticjustice.deeppoemsinc.domain.models.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import lombok.*;

@Document("pending_ips")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingIp {
    @Id
    private String id;
    private String userId;
    private String ipAddress;
    private LocalDateTime detectedAt;
    private String status; // PENDING, APPROVED, REJECTED

    // getters/setters
    // ...
}