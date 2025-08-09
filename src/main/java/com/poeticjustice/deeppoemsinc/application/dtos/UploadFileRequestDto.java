package com.poeticjustice.deeppoemsinc.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadFileRequestDto {  
    
    private String category = "Documents"; // Default category
    
    // @NotBlank(message = "User ID must not be empty")
    // @NotBlank(message = "Category must not be empty")
    // @Size(max = 50, message = "Category must not exceed 50 characters")
    private String userId = "12345"; // Default user ID
    
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