package com.s18947440.CPEN431.A2.rr;

import ca.NetSysLab.ProtocolBuffers.Message; //
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.*;
import java.util.zip.CRC32;

import static com.s18947440.CPEN431.A2.rr.Checksum.crc32;

public class RequestReplyClient {
    private final InetAddress serverAddr;
    private final int serverPort;
    private final DatagramSocket socket;

    public RequestReplyClient(String host, int port) throws IOException {
        this.serverAddr = InetAddress.getByName(host);
        this.serverPort = port;
        this.socket = new DatagramSocket();
    }

    public byte[] requestReply(byte[] appPayload) throws IOException {
        byte[] messageId = new byte[16]; // 16 bytes

        long checksum = crc32(messageId, appPayload); // CRC32(messageID +++ payload):contentReference[oaicite:8]{index=8}

        Message.Msg req =
                Message.Msg.newBuilder()
                        .setMessageID(ByteString.copyFrom(messageId))
                        .setPayload(ByteString.copyFrom(appPayload))
                        .setCheckSum(checksum)
                        .build();

        byte[] reqBytes = req.toByteArray();

        int timeoutMs = 100;  // start (tune if RTT high)
        for (int attempt = 0; attempt < 4; attempt++) {
            DatagramPacket p = new DatagramPacket(reqBytes, reqBytes.length, serverAddr, serverPort);
            socket.send(p);

            try {
                socket.setSoTimeout(timeoutMs);

                byte[] buf = new byte[16 * 1024 + 64];
                DatagramPacket reply = new DatagramPacket(buf, buf.length);
                socket.receive(reply);

                // IMPORTANT: parse only reply.getLength() bytes (protobuf parse needs exact length):contentReference[oaicite:9]{index=9}
                byte[] replyBytes = new byte[reply.getLength()];
                System.arraycopy(reply.getData(), reply.getOffset(), replyBytes, 0, reply.getLength());

                Message.Msg rep = Message.Msg.parseFrom(replyBytes);

                byte[] repId = rep.getMessageID().toByteArray();
                byte[] repPayload = rep.getPayload().toByteArray();
                long repChecksum = rep.getCheckSum();

                // 1) messageID must match
                if (!java.util.Arrays.equals(messageId, repId)) {
                    // treat as not our reply -> keep waiting/retry
                    throw new SocketTimeoutException();
                }

                // 2) checksum verify, else drop + retry:contentReference[oaicite:10]{index=10}
                long expected = crc32(repId, repPayload);
                if (repChecksum != expected) {
                    throw new SocketTimeoutException();
                }

                return repPayload;

            } catch (SocketTimeoutException e) {
                timeoutMs *= 2;
            }
        }

        throw new IOException("No valid reply after retries");
    }


}


