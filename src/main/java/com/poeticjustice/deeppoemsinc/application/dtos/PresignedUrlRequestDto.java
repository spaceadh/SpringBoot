package com.poeticjustice.deeppoemsinc.application.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresignedUrlRequestDto {
    @NotBlank
    private String userId;
    @NotBlank
    private String fileName;
    @NotNull
    private Long fileSize;
    private String category;
    private String client; // "jedis" or "lettuce"
    // getters/setters
}
