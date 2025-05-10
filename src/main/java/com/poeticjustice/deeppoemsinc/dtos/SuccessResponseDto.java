package com.poeticjustice.deeppoemsinc.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponseDto {

    private int status;
    private String message;
    private String url;
}