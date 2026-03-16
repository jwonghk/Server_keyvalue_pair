package com.s18947440.CPEN431.A2.app;

import ca.NetSysLab.ProtocolBuffers.RequestPayload;
import ca.NetSysLab.ProtocolBuffers.ResponsePayload;
import com.s18947440.CPEN431.A2.rr.RequestReplyClient;

import java.io.IOException;

public class SecretKeyClient {
    private final RequestReplyClient rr;

    public SecretKeyClient(RequestReplyClient rr) {
        this.rr = rr;
    }

    public byte[] getSecretCode(int studentId) throws IOException {
        byte[] reqPayload =
                RequestPayload.ReqPayload.newBuilder()
                        .setStudentID(studentId)
                        .build()
                        .toByteArray();

        byte[] repPayload = rr.requestReply(reqPayload);

        // parse application payload properly (don’t “read bytes directly”):contentReference[oaicite:11]{index=11}
        ResponsePayload.ResPayload resp =
                ResponsePayload.ResPayload.parseFrom(repPayload);

        return resp.getSecretKey().toByteArray();
    }
}
