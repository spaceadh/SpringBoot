package com.poeticjustice.deeppoemsinc.exceptions;

public class InvalidJwtTokenException extends RuntimeException {
    public InvalidJwtTokenException(String message) {
        super(message);
    }
}
