package com.s18947440.CPEN431.A2.rr;


import ca.NetSysLab.ProtocolBuffers.Message;
import ca.NetSysLab.ProtocolBuffers.RequestPayload;
import com.google.protobuf.ByteString;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class RequestReplyServer {

        private DatagramSocket socket;
        private RequestCache requestCache;


        public RequestReplyServer(int port) throws Exception {
            socket = new DatagramSocket(port);
            requestCache = new RequestCache();
        }



        public void start() throws Exception {
            byte[] buf = new byte[16*1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while(true) {


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
                };

                // check cache to see if the request has been replied before
                byte[] cachedReply = requestCache.get(Arrays.toString(MessageId));

                /*
                if (cachedReply != null) {
                    sendReply(cachedReply, packet);
                    continue;
                }

                // handle the request
                byte[] replyPayload = handleRequest(PayLoad);
                */

                /*
                long checkSum = Checksum.crc32(MessageId, replyPayload);
                Message.Msg reply = Message.Msg.newBuilder()
                        .setMessageID(ByteString.copyFrom(MessageId))
                        .setPayload(ByteString.copyFrom(replyPayload))
                        .setCheckSum(checkSum)
                        .build();



                byte[] replyBytes = reply.toByteArray();

                String idKey = Arrays.toString(MessageId);
                requestCache.put(idKey, replyBytes);


                 */

            }


        }




}
