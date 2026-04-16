package com.s18947440.CPEN431.A2.app;

import ca.NetSysLab.ProtocolBuffers.KeyValueRequest;
import ca.NetSysLab.ProtocolBuffers.KeyValueResponse;
import com.google.protobuf.ByteString;
import com.s18947440.CPEN431.A2.rr.RequestReplyClient;

import static sun.misc.Version.print;

public class ClientAppTestingLocalServer {
    public static void main(String[] args) throws Exception {
        // local Host during testing : String serverHost = "localhost";
        // String serverHost = "localhost";
        // EC2 public IP: String serverHost = "3.137.142.226";
        System.out.println("=== NEW CLIENT RUNNING ===");
        String serverHost = "54.196.24.30"; //54.196.24.30
        int serverPort = 3102;

        RequestReplyClient reqRepClient = new RequestReplyClient(serverHost, serverPort);

        if (args.length < 3) {
            System.out.println("Usage: java ClientAppTestingLocalServer <key> <value>");
            return;
        }

        String keyStr = args[0];
        String valueStr = args[1];
        String commandCode = args[2];


        System.out.println("Client's keyStr command value: " +  keyStr);
        System.out.println("Client's valueStr command value: " +  valueStr);
        System.out.println("Client's commandCode command value: " +  commandCode);
        // Testing alive
        KeyValueRequest.KVRequest putReq =
                KeyValueRequest.KVRequest.newBuilder()
                        .setCommand(Integer.parseInt(commandCode)) // PUT
                        .setKey(ByteString.copyFromUtf8(keyStr))
                        .setValue(ByteString.copyFromUtf8(valueStr))
                        .setVersion(7)
                        .build();
        byte[] replyPayload = reqRepClient.requestReply(putReq.toByteArray());

        KeyValueResponse.KVResponse reply =
                KeyValueResponse.KVResponse.parseFrom(replyPayload);

        System.out.println("replyPayload length = " + replyPayload.length);
        System.out.println("errCode = " + reply.getErrCode());
        System.out.println("hasVersion = " + reply.hasVersion());
        System.out.println("version = " + reply.getVersion());
        /*

        // Testing PUT
        KeyValueRequest.KVRequest putReq =
                KeyValueRequest.KVRequest.newBuilder()
                        .setCommand(1) // PUT
                        .setKey(ByteString.copyFromUtf8(keyStr))
                        .setValue(ByteString.copyFromUtf8(valueStr))
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
                        .setKey(ByteString.copyFromUtf8(keyStr))
                        .build();

        byte[] getReplyPayload = reqRepClient.requestReply(getReq.toByteArray());

        KeyValueResponse.KVResponse getReply =
                KeyValueResponse.KVResponse.parseFrom(getReplyPayload);

        System.out.println("GET errCode = " + getReply.getErrCode());
        System.out.println("GET value = " + getReply.getValue().toStringUtf8());
        System.out.println("GET version = " + getReply.getVersion());

         */
    }
}