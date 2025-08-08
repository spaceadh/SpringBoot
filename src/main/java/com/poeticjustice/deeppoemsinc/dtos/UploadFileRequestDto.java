package com.poeticjustice.deeppoemsinc.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileRequestDto {  
    @NotBlank(message = "Category must not be empty")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;
    
    @NotBlank(message = "User ID must not be empty")
    private String userId;
    
    @NotBlank(message = "Client must not be empty")
    @Pattern(regexp = "jedis|lettuce", message = "Client must be 'jedis' or 'lettuce'")
    private String client;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadFileResponseDto {
        private String message;
        private String storageUrl;
        private HttpStatus status;
    }
}