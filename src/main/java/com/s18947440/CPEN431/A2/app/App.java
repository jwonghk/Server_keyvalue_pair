package com.s18947440.CPEN431.A2.app;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) throws IOException {

        System.out.println("Hello World!\n");

        String currentHostAddr;
        String dest_hostname = "35.95.139.9";
        //String dest_hostname = "34.213.181.35";


        int dest_port = 43102; // for a1: int dest_port = 43101;

        UDP_client udpc = new UDP_client(dest_hostname, dest_port);

        ByteBuffer b = ByteBuffer.allocate(32);
        //b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(258);
        //b.putInt(210);
        //b.putInt(127);
        //b.putInt(129);


        byte[] result = b.array();
        System.out.println(result[0]);
        System.out.println(result[1]);
        System.out.println(result[2]);
        System.out.println(result[3]);
        System.out.println(result[4]);


        long uniq_time = System.nanoTime();
        ByteBuffer byBf = ByteBuffer.allocate(16);
        byBf.order(ByteOrder.BIG_ENDIAN);




        currentHostAddr = InetAddress.getLocalHost().getHostName();
        byte[] curH = InetAddress.getLocalHost().getAddress();
        //byte[] stringByteArray = "207.1".getBytes() ; //currentHostAddr.getBytes();
        b.put(curH);

        System.out.println("Sapcing!!!!!!!!!");
       /* System.out.println(result[0]);
        System.out.println(result[1]);
        System.out.println(result[2]);
        System.out.println(result[3]);
        System.out.println(result[4]);
        System.out.println(result[5]);
        System.out.println(result[6]);
        System.out.println(result[7]);
        System.out.println(result[8]);
        System.out.println(result[9]);
        System.out.println(result[10]);
        System.out.println(result[11]);
        System.out.println(result[12]);
        System.out.println(result[13]);
        System.out.println(result[14]);
        System.out.println(result[15]);
        System.out.println(result[16]);
        System.out.println(result[17]);
        System.out.println(result[18]);
        System.out.println(result[19]);*/
        System.out.println("local host address " + InetAddress.getLocalHost().getHostAddress());


        //System.out.println(InetAddress.getHostName());

        //DatagramSocket socket = new DatagramSocket();
        //SocketAddress localAddress = socket.getLocalSocketAddress();
        //int localPort = socket.getLocalPort();


        DatagramSocket socket2 = new DatagramSocket();
        InetAddress address2 = InetAddress.getByName("localhost");
        /*System.out.println("Datagram: local host address is  " + localAddress);
        System.out.println("Datagram: port is  " + localPort);

        System.out.println("S2: local host address is  " + address2);
        System.out.println("S2: port is  " + localPort);
        */


    }
}
