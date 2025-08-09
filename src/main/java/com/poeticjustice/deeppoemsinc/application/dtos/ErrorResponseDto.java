package com.poeticjustice.deeppoemsinc.application.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponseDto {
    private int status;
    private String errorCode;
    private String message;
}