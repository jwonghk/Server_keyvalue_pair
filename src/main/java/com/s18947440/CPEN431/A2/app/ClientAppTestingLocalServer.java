package com.s18947440.CPEN431.A2.app;

import ca.NetSysLab.ProtocolBuffers.KeyValueRequest;
import ca.NetSysLab.ProtocolBuffers.KeyValueResponse;
import com.google.protobuf.ByteString;
import com.s18947440.CPEN431.A2.rr.RequestReplyClient;

public class ClientAppTestingLocalServer {
    public static void main(String[] args) throws Exception {
        String serverHost = "localhost";
        int serverPort = 3102;

        RequestReplyClient reqRepClient = new RequestReplyClient(serverHost, serverPort);

        // Testing PUT
        KeyValueRequest.KVRequest putReq =
                KeyValueRequest.KVRequest.newBuilder()
                        .setCommand(1) // PUT
                        .setKey(ByteString.copyFromUtf8("a"))
                        .setValue(ByteString.copyFromUtf8("hello"))
                        .setVersion(731)
                        .build();

        byte[] replyPayload = reqRepClient.requestReply(putReq.toByteArray());

        KeyValueResponse.KVResponse reply =
                KeyValueResponse.KVResponse.parseFrom(replyPayload);

        System.out.println("replyPayload length = " + replyPayload.length);
        System.out.println("errCode = " + reply.getErrCode());
        System.out.println("hasVersion = " + reply.hasVersion());
        System.out.println("version = " + reply.getVersion());


        // Testing GET
        KeyValueRequest.KVRequest getReq =
                KeyValueRequest.KVRequest.newBuilder()
                        .setCommand(2) // GET
                        .setKey(ByteString.copyFromUtf8("a"))
                        .build();

        byte[] getReplyPayload = reqRepClient.requestReply(getReq.toByteArray());

        KeyValueResponse.KVResponse getReply =
                KeyValueResponse.KVResponse.parseFrom(getReplyPayload);

        System.out.println("GET errCode = " + getReply.getErrCode());
        System.out.println("GET value = " + getReply.getValue().toStringUtf8());
        System.out.println("GET version = " + getReply.getVersion());
    }
}