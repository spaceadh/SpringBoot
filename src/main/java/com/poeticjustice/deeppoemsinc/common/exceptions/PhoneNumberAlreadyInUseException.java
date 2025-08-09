package com.poeticjustice.deeppoemsinc.common.exceptions;

public class PhoneNumberAlreadyInUseException extends RuntimeException {
    public PhoneNumberAlreadyInUseException(String message) {
        super(message);
    }
}