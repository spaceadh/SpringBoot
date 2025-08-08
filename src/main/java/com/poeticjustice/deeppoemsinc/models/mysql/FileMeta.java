package com.poeticjustice.deeppoemsinc.models.mysql;

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
    private LocalDateTime uploadedAt;

    // Getters and setters
}
