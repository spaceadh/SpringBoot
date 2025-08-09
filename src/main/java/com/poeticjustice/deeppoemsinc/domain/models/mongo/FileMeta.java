package com.poeticjustice.deeppoemsinc.domain.models.mongo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String category;
    private long sizeInBytes;
    private String userId;
    private String storageUrl;
    private String bucketName;
    private String objectKey;
    private boolean isPublic;
    // @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
    // @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    private boolean isDeleted = false;
    private boolean isProcessed = false;
}