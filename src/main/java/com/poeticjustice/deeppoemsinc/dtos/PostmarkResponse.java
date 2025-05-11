package com.poeticjustice.deeppoemsinc.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostmarkResponse {
    private int ErrorCode;
    private String Message;
    private String MessageID;

    // Getters and setters
    public int getErrorCode() { return ErrorCode; }
    public void setErrorCode(int errorCode) { ErrorCode = errorCode; }
    public String getMessage() { return Message; }
    public void setMessage(String message) { Message = message; }
    public String getMessageID() { return MessageID; }
    public void setMessageID(String messageID) { MessageID = messageID; }
}