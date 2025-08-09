package com.poeticjustice.deeppoemsinc.common.exceptions;

public class LacksAuthorizationHeader extends RuntimeException {
    public LacksAuthorizationHeader(String message) {
        super(message);
    }
}
