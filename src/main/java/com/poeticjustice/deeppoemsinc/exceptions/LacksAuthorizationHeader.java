package com.poeticjustice.deeppoemsinc.exceptions;

public class LacksAuthorizationHeader extends RuntimeException {
    public LacksAuthorizationHeader(String message) {
        super(message);
    }
}
