package com.poeticjustice.deeppoemsinc.exceptions;

public class PhoneNumberAlreadyInUseException extends RuntimeException {
    public PhoneNumberAlreadyInUseException(String message) {
        super(message);
    }
}