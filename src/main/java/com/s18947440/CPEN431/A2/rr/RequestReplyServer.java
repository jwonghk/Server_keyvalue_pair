package com.s18947440.CPEN431.A2.rr;


import ca.NetSysLab.ProtocolBuffers.KeyValueRequest;
import ca.NetSysLab.ProtocolBuffers.KeyValueResponse;
import ca.NetSysLab.ProtocolBuffers.Message;
import ca.NetSysLab.ProtocolBuffers.RequestPayload;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;


import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class RequestReplyServer {

        private DatagramSocket socket;
        private RequestCache requestCache;
        private Map<ByteString, ValueEntry> keyValueStore;
        private int currentBytes = 0; // storing the total memory (in Bytes) usages of the key-value pair in
                                    // the keyValueStore Map
        private static final int MAX_BYTES = 64 * 1024 * 1024; // limit of total memory size of keyValueStore

        public RequestReplyServer(int port) throws Exception {
            socket = new DatagramSocket(port);
            requestCache = new RequestCache();
            keyValueStore = new HashMap<>();
        }

        private void sendReply(byte[] replyBytes, DatagramPacket requestPacket) throws java.io.IOException {
            DatagramPacket replyPacket = new DatagramPacket(
                replyBytes,
                replyBytes.length,
                requestPacket.getAddress(),
                requestPacket.getPort()
            );
            socket.send(replyPacket);
        }

        public void start() throws Exception {
            byte[] buf = new byte[16 * 1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while (true) {


                packet.setLength(buf.length);   // reset capacity
                socket.receive(packet);
                byte[] requestBytes = Arrays.copyOfRange(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getOffset() + packet.getLength()
                );

                Message.Msg req = Message.Msg.parseFrom(requestBytes);

                byte[] MessageId = req.getMessageID().toByteArray();
                byte[] PayLoad = req.getPayload().toByteArray();

                // checksum verification
                long expected = Checksum.crc32(MessageId, PayLoad);
                if (req.getCheckSum() != expected) {
                    continue;
                }
                ;

                // check cache to see if the request has been replied before
                byte[] cachedReply = requestCache.get(Arrays.toString(MessageId));

                System.out.println("SERVER messageID = " + Arrays.toString(MessageId));
                System.out.println("SERVER payload command = " + KeyValueRequest.KVRequest.parseFrom(PayLoad).getCommand());

                // For At-Most-Once semantics
                if (cachedReply != null) {
                    sendReply(cachedReply, packet);
                    continue;
                }

                // handle the request
                byte[] replyPayload = handleRequest(PayLoad);


                long checkSum = Checksum.crc32(MessageId, replyPayload);
                Message.Msg reply = Message.Msg.newBuilder()
                        .setMessageID(ByteString.copyFrom(MessageId))
                        .setPayload(ByteString.copyFrom(replyPayload))
                        .setCheckSum(checkSum)
                        .build();


                byte[] replyBytes = reply.toByteArray();

                String idKey = Arrays.toString(MessageId);
                requestCache.put(idKey, replyBytes);

                /*
                packet.setData(replyBytes);
                socket.send(packet);*/
                sendReply(replyBytes, packet);
            }
        }



        public byte[] handleRequest(byte[] requestedPayload) throws InvalidProtocolBufferException {


            try {
                KeyValueRequest.KVRequest req =
                        KeyValueRequest.KVRequest.parseFrom(requestedPayload);


                int reqCode = req.getCommand();
                switch (reqCode) {
                    case 1: { // PUT
                        if (!req.hasKey() || req.getKey().size() == 0 || req.getKey().size() > 32) {

                            KeyValueResponse.KVResponse resp =
                                    KeyValueResponse.KVResponse.newBuilder()
                                            .setErrCode(6)
                                            .build();
                            return resp.toByteArray();
                        }

                        if (!req.hasValue() || req.getValue().size() > 10000) {
                            KeyValueResponse.KVResponse resp =
                                    KeyValueResponse.KVResponse.newBuilder()
                                            .setErrCode(7)
                                            .build();
                            return resp.toByteArray();
                        }

                        System.out.println("Inside PUT in the RequestReplyServer.java file");
                        ByteString key = req.getKey();
                        ByteString value = req.getValue();
                        int version = req.hasVersion() ? req.getVersion() : 0;

                        int newSize = key.size() + value.size();

                        ValueEntry oldEntry = keyValueStore.get(key);
                        int oldSize = 0;
                        if (oldEntry != null) {
                            oldSize = key.size() + oldEntry.value.size();
                        }

                        if (currentBytes - oldSize + newSize > MAX_BYTES) {
                            KeyValueResponse.KVResponse resp =
                                    KeyValueResponse.KVResponse.newBuilder()
                                            .setErrCode(2)
                                            .build();
                            return resp.toByteArray();
                        }

                        keyValueStore.put(key, new ValueEntry(value, version));
                        currentBytes = currentBytes - oldSize + newSize;

                        KeyValueResponse.KVResponse resp =
                                KeyValueResponse.KVResponse.newBuilder()
                                        .setErrCode(0)
                                        .setVersion(version)
                                        .build();

                        return resp.toByteArray();
                    }
                    case 2: // for the Get command
                        if (!req.hasKey() || req.getKey().size() == 0 || req.getKey().size() > 32) {
                            KeyValueResponse.KVResponse resp2 =
                                    KeyValueResponse.KVResponse.newBuilder()
                                            .setErrCode(6)
                                            .build();
                            return resp2.toByteArray();
                        }


                        System.out.println("Inside GET in the RequestReplyServer.java file");
                        ByteString key2 = req.getKey();
                        ValueEntry entry = keyValueStore.get(key2);

                        KeyValueResponse.KVResponse.Builder resp2 =
                                KeyValueResponse.KVResponse.newBuilder();

                        System.out.println("SERVER VALUE during GET = " + entry.value.toStringUtf8());

                        if (entry != null) {
                            resp2.setErrCode(0)
                                    .setValue(entry.value)
                                    .setVersion(entry.version);
                        } else {
                            resp2.setErrCode(1); // key not found
                        }
                        return resp2.build().toByteArray();

                    case 3: // for removal
                        if (!req.hasKey() || req.getKey().size() == 0 || req.getKey().size() > 32) {
                            KeyValueResponse.KVResponse resp3 =
                                    KeyValueResponse.KVResponse.newBuilder()
                                            .setErrCode(6)
                                            .build();
                            return resp3.toByteArray();
                        }
                        ByteString key3 = req.getKey();
                        ValueEntry valRemoved = keyValueStore.remove(key3);

                        KeyValueResponse.KVResponse.Builder resp3 =
                                KeyValueResponse.KVResponse.newBuilder();

                        if (valRemoved != null) {
                            resp3.setErrCode(0);
                        } else {
                            resp3.setErrCode(1); // key not found
                        }
                        return resp3.build().toByteArray();

                    case 4: // for the ShutDown
                        KeyValueResponse.KVResponse resp4 =
                                KeyValueResponse.KVResponse.newBuilder()
                                        .setErrCode(0)
                                        .build();

                        // send reply FIRST, then shutdown
                        new Thread(() -> {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException ignored) {

                            }
                            System.exit(0);
                        }).start();
                        return resp4.toByteArray();

                    case 5: // deletes all keys stored in the node
                        keyValueStore.clear();
                        currentBytes = 0;
                        KeyValueResponse.KVResponse resp5 =
                                KeyValueResponse.KVResponse.newBuilder()
                                        .setErrCode(0)
                                        .build();
                        return resp5.toByteArray();

                    case 6: //  IsAlive: does nothing but replies with success if the node is alive.
                        KeyValueResponse.KVResponse resp6 =
                                KeyValueResponse.KVResponse.newBuilder()
                                        .setErrCode(23456)
                                        .build();
                        return resp6.toByteArray();

                    case 7: //  GetPID: the node is expected to reply with the processID of the Java process
                        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
                        int pid = Integer.parseInt(jvmName.split("@")[0]);

                        KeyValueResponse.KVResponse resp7 =
                                KeyValueResponse.KVResponse.newBuilder()
                                        .setErrCode(0)
                                        .setPid(pid)
                                        .build();
                        return resp7.toByteArray();

                    case 8: // GetMembershipCount: the node is expected to reply
                        // with the count of the currently active participants known by the local node.
                        KeyValueResponse.KVResponse resp8 =
                                KeyValueResponse.KVResponse.newBuilder()
                                        .setErrCode(0)
                                        .setMembershipCount(1)
                                        .build();

                        return resp8.toByteArray();

                    default: // 0x05 Unrecognized command
                        KeyValueResponse.KVResponse respDefault =
                                KeyValueResponse.KVResponse.newBuilder()
                                        .setErrCode(5)
                                        .build();

                        return respDefault.toByteArray();


                }
            } catch (Exception e) {
                e.printStackTrace();
                return KeyValueResponse.KVResponse.newBuilder()
                        .setErrCode(4)
                        .build()
                        .toByteArray();


            }
        };



        private static class ValueEntry {
            ByteString value;
            int version;
            ValueEntry(ByteString value, int version) {
                this.value = value;
                this.version = version;
        }
    };







    /*TEST1: testting the compiled code of KeyValueRequest's various command like getCommand, etc*/

    /*
    public byte[] handleRequest(byte[] requestedPayload) {
        try {
            KeyValueRequest.KVRequest req =
                    KeyValueRequest.KVRequest.parseFrom(requestedPayload);

            System.out.println("command = " + req.getCommand());

            if (req.hasKey()) {
                System.out.println("hasKey = true");
                System.out.println("key = " + req.getKey().toStringUtf8());
            } else {
                System.out.println("hasKey = false");
            }

            if (req.hasValue()) {
                System.out.println("hasValue = true");
                System.out.println("value = " + req.getValue().toStringUtf8());
            } else {
                System.out.println("hasValue = false");
            }

            if (req.hasVersion()) {
                System.out.println("hasVersion = true");
                System.out.println("version = " + req.getVersion());
            } else {
                System.out.println("hasVersion = false");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new byte[0];
    }
    */
}

