package com.poeticjustice.deeppoemsinc.domain.models.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "AcceptedIps")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AcceptedIps {

    @Id
    private String id;
    private String userId;
    private String[] ipAddress;
}