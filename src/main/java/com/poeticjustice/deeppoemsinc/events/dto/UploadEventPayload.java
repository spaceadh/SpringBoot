package com.poeticjustice.deeppoemsinc.events.dto;

import java.io.Serializable;

/**
 * DTO for upload event payload.
 */
public class UploadEventPayload implements Serializable {
    private String userId;
    private String fileName;
    private String category;

    public UploadEventPayload(String userId, String fileName, String category) {
        this.userId = userId;
        this.fileName = fileName;
        this.category = category;
    }

    public String getUserId() {
        return userId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getCategory() {
        return category;
    }
}
