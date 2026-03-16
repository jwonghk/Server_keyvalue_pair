package com.s18947440.CPEN431.A2.rr;

import java.util.zip.CRC32;

public class Checksum {

    private Checksum() {}

    public static long crc32(byte[] messageId, byte[] payload) {
        CRC32 crc = new CRC32();
        crc.update(messageId);
        crc.update(payload);
        return crc.getValue(); // long
    }

}
