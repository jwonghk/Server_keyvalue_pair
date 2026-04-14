package com.s18947440.CPEN431.A2.app;

import ca.NetSysLab.ProtocolBuffers.KeyValueRequest;
import com.google.protobuf.ByteString;
import com.s18947440.CPEN431.A2.rr.RequestReplyServer;
import jdk.nashorn.internal.ir.RuntimeNode;

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
    public static void main( String[] args ) throws Exception {

        RequestReplyServer requestReplyServer = new RequestReplyServer(3102);
        System.out.println("App starting");
        System.out.println("Max heap: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        requestReplyServer.start();

    }
};
