package com.s18947440.CPEN431.A2.rr;

import java.security.SecureRandom;

public final class MessageId {
    private static final SecureRandom RNG = new SecureRandom();

    private MessageId() {}

    /** Returns a fresh 16-byte request ID. */
    public static byte[] newId16Bytes() {
        byte[] id = new byte[16];
        RNG.nextBytes(id);
        return id;
    }
}