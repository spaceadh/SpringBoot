package com.poeticjustice.deeppoemsinc.events;

import java.io.Serializable;
import java.util.Map;

import lombok.*;
import jakarta.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadEventPayload implements Serializable {
   private String fileId;
   private String fileName;
   private String fileType;
   private String uploaderId;
   private long uploadDate;
   private UploadStatus status;
   private Map<String, String> metadata;

   public enum UploadStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}
