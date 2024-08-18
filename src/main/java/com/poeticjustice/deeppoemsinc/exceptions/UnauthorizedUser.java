package com.poeticjustice.deeppoemsinc.exceptions;

public class UnauthorizedUser extends RuntimeException {
    public UnauthorizedUser(String message) {
        super(message);
    }
}
