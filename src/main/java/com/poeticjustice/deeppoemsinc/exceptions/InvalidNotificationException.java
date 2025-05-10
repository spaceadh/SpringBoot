package com.poeticjustice.deeppoemsinc.exceptions;

public class InvalidNotificationException extends RuntimeException {

    private final String errorCode;

    public InvalidNotificationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}