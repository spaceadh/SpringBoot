package com.poeticjustice.deeppoemsinc.dtos;

import lombok.Data;

import java.util.List;

@Data
public class ATSMSResponse {
    private SMSMessageData SMSMessageData;

    @Data
    public static class SMSMessageData {
        private String Message;
        private List<Recipient> Recipients;
    }

    @Data
    public static class Recipient {
        private int statusCode;
        private String number;
        private String cost;
        private String messageId;
    }
}