package com.s18947440.CPEN431.A2.app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import ca.NetSysLab.ProtocolBuffers.Message;
import ca.NetSysLab.ProtocolBuffers.RequestPayload;
import ca.NetSysLab.ProtocolBuffers.ResponsePayload;
import com.google.protobuf.ByteString;
import com.s18947440.CPEN431.A2.rr.Checksum;


public class UDP_client {



    //int port;
    //InetAddress address;
    //String hostname = "34.213.181.35";

    DatagramSocket socket = new DatagramSocket();
    DatagramPacket packet;
    byte[] sendBuf = new byte[256];
    String messageToServer;

    InetAddress ia;

    public UDP_client(String hostname, int port) throws IOException {
        ia = InetAddress.getByName(hostname);
        System.out.println(ia.getHostName());



        int localPort = socket.getLocalPort();
        //System.out.println(portLocal);
        //int localPort =  12789 ;//socket.getLocalPort();
        byte b0 = (byte) (localPort & 0Xff);
        byte b1 = (byte) ((localPort >> 8) & 0xff);
        //byte b2 = (byte) ((localPort >> 16) & 0xff);
        //byte b3 = (byte) ((localPort >> 24) & 0xff);
        //byte b1 = (byte) (localPort & 0Xff);

        System.out.println("value of localport: " + localPort);
        System.out.printf("value of localport: %4x \n",  b0);
        System.out.printf("value of localport: %4x \n",  b1);
        //System.out.printf("value of localport: %4x \n",  b2);
        //System.out.printf("value of localport: %4x \n",  b3);



        // Build the PayLoad body of the request message
        int studentId = 1381632; //1381632
        byte id = (byte) studentId;

        // 1)
        socket.connect(ia, port);

        byte[] clientIP = socket.getLocalAddress().getAddress();
        int clientPort = socket.getLocalPort();
        if (clientIP.length != 4) {
            throw new IllegalStateException("Need IPv4 address, got: " + socket.getLocalAddress());
        }

        // 2) Random 2 bytes
        short rnd = (short) new SecureRandom().nextInt(1 << 16);

        // 3) time of request
        long timeofReq = System.nanoTime();

        // 4) the whole message
        byte[] messageId = new byte[16];
        new SecureRandom().nextBytes(messageId);

        byte[] appPayload =
                RequestPayload.ReqPayload.newBuilder()
                        .setStudentID(studentId)
                        .build()
                        .toByteArray();


        long checkSum = Checksum.crc32(messageId, appPayload);


        Message.Msg req =
                Message.Msg.newBuilder()
                        .setMessageID(ByteString.copyFrom(messageId))
                        .setPayload(ByteString.copyFrom(appPayload))
                        .setCheckSum(checkSum)
                        .build();


        byte[] requestBytes = req.toByteArray();
        System.out.printf("value of client IP: %s%n \n",  socket.getLocalAddress().getHostAddress());
        System.out.printf("value of client port (dec): %d%n \n",  clientPort);
        System.out.printf("value of local time: %d%n \n",  timeofReq);
        System.out.println("Instant: " + java.time.Instant.now());

        packet = new DatagramPacket(requestBytes, requestBytes.length, ia, port);

        System.out.println("Sending to: " + ia.getHostAddress() + ":" + port);
        System.out.println("Local: " + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());
        System.out.println("Request length: " + requestBytes.length);



        byte[] recbuf = new byte[16 * 1024 + 64];
        DatagramPacket reply = new DatagramPacket(recbuf, recbuf.length);

        int timeoutMs = 100;
        byte[] repPayload = null;   // will hold the valid payload when we succeed

        for (int attempt = 0; attempt < 4; attempt++) {
            socket.send(packet);

            try {
                socket.setSoTimeout(timeoutMs);

                // receive one UDP datagram
                reply.setLength(recbuf.length);
                socket.receive(reply);

                // parse exactly the received bytes
                byte[] replyBytes = Arrays.copyOfRange(
                        reply.getData(),
                        reply.getOffset(),
                        reply.getOffset() + reply.getLength()
                );

                Message.Msg rep = Message.Msg.parseFrom(replyBytes);

                // 1) ID check
                byte[] recvId = rep.getMessageID().toByteArray();

                if(Arrays.equals(messageId, recvId)) {
                    System.out.println("Message Id being: " + Arrays.toString(messageId) );
                    System.out.println("Received ID being: " + Arrays.toString(recvId));
                }
                if (!Arrays.equals(messageId, recvId)) {
                    throw new SocketTimeoutException(); // treat as "not our valid reply"
                }

                // 2) checksum check
                repPayload = rep.getPayload().toByteArray();
                long expected = Checksum.crc32(recvId, repPayload);
                if (rep.getCheckSum() != expected) {
                    throw new SocketTimeoutException(); // corrupted/invalid -> retry
                }

                // ✅ valid reply
                break;

            } catch (SocketTimeoutException e) {
                timeoutMs *= 2;
                if (attempt == 3) {
                    throw new IOException("No valid reply after 4 tries (timeouts/invalid packets)");
                }
            }
        }

        // At this point repPayload is valid (if we didn't throw)
        ResponsePayload.ResPayload resp = ResponsePayload.ResPayload.parseFrom(repPayload);
        byte[] secretBytes = resp.getSecretKey().toByteArray();
        System.out.println("Secret key (hex): " + bytesToHex(secretBytes, 0, secretBytes.length));


    }

    /*
    private static String bytesToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) sb.append(String.format("%02x", b & 0xFF));
        return sb.toString();
    }*/

    static String toHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    private static String bytesToHex(byte[] a, int off, int len) {
        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) sb.append(String.format("%02x", a[off + i] & 0xFF));
        return sb.toString();
    }
};


