package com.poeticjustice.deeppoemsinc.application.dtos;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadEventPayload implements Serializable {
   private String fileId;
   private String fileName;
   private String fileType;
   private String uploaderId;
   private long uploadDate;
   private UploadStatus status;
   private Map<String, String> metadata;
    private String objectKey;

   public enum UploadStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}
