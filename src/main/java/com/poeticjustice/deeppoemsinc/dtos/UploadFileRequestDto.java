package com.poeticjustice.deeppoemsinc.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileRequestDto {  
    
    private String category = "Documents"; // Default category
    
    @NotBlank(message = "User ID must not be empty")
    // @NotBlank(message = "Category must not be empty")
    // @Size(max = 50, message = "Category must not exceed 50 characters")
    private String userId;
    
    private String client = "lettuce"; // Default to lettuce
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadFileResponseDto {
        private String message;
        private String storageUrl;
        private HttpStatus status;
    }
}