package com.poeticjustice.deeppoemsinc.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ATSMSResponse {
    @JsonProperty("SMSMessageData")
    private SMSMessageData smsMessageData;

    @Data
    public static class SMSMessageData {
        @JsonProperty("Message")
        private String message;
        
        @JsonProperty("Recipients")
        private List<Recipient> recipients;

        @JsonProperty("status")
        private String status;
    }

    @Data
    public static class Recipient {
        @JsonProperty("statusCode")
        private int statusCode;
        
        @JsonProperty("number")
        private String number;
        
        @JsonProperty("cost")
        private String cost;
        
        @JsonProperty("messageId")
        private String messageId;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("messageParts")
        private int messageParts;
    }
}