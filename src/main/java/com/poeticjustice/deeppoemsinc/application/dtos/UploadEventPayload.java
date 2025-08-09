package com.poeticjustice.deeppoemsinc.application.dtos;

import java.io.Serializable;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class UploadEventPayload implements Serializable {
    private String userId;
    private String fileName;
    private String category;
}
