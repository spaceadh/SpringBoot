package com.poeticjustice.deeppoemsinc.common.exceptions;

public class JwtTokenCreationException extends RuntimeException {
    public JwtTokenCreationException(String message) {
        super(message);
    }
}