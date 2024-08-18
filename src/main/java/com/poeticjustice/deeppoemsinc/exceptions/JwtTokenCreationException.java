package com.poeticjustice.deeppoemsinc.exceptions;

public class JwtTokenCreationException extends RuntimeException {
    public JwtTokenCreationException(String message) {
        super(message);
    }
}