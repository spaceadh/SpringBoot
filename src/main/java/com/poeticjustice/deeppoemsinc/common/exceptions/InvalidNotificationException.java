package com.poeticjustice.deeppoemsinc.common.exceptions;

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