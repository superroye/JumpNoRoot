package com.wolf.jumpnoroot.net;

import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Created by Roye on 2018/1/11.
 */

public class JumpServer {
    private static final int BUF_SIZE = 1024;
    private static final int PORT = 8089;
    private static final int TIMEOUT = 3000;

    ServerSocketChannel ssc = null;
    String command;

    String addr;

    public JumpServer() {
        new Thread() {
            @Override
            public void run() {
                getAddress();
                selector();
            }
        }.start();
    }

    public void sendCommand(String command) {
        this.command = command;
    }

    public String getAddress() {
        if (addr != null) {
            return addr;
        }
        addr = getHostIP() + ":" + PORT;
        return addr;
    }


    public void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssChannel.accept();
        sc.configureBlocking(false);

        sc.register(key.selector(), SelectionKey.OP_WRITE, ByteBuffer.allocateDirect(BUF_SIZE));
    }

    public void handleRead(SelectionKey key) throws IOException {
        System.out.print("=== handleRead key");
        SocketChannel sc = (SocketChannel) key.channel();

        ByteBuffer buf = (ByteBuffer) key.attachment();
        long bytesRead = sc.read(buf);
        while (bytesRead > 0) {
            buf.flip();
            while (buf.hasRemaining()) {
                System.out.print((char) buf.get());
            }
            System.out.println();
            buf.clear();
            bytesRead = sc.read(buf);
        }
    }

    public void handleWrite(SelectionKey key) throws IOException {
        if (command == null) {
            return;
        }
        ByteBuffer buf = (ByteBuffer) key.attachment();
        buf.clear();
        buf.put(command.getBytes());
        command = null;
        buf.flip();
        SocketChannel sc = (SocketChannel) key.channel();
        sc.write(buf);
    }

    public void selector() {
        Selector selector = null;
        try {
            selector = Selector.open();
            ssc = ServerSocketChannel.open();

            ssc.socket().bind(new InetSocketAddress(PORT));
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();

                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    iter.remove();

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        if (sc.isConnected()) {
                            handleRead(key);
                        } else {
                            System.out.println("is not connected!");
                        }
                    } else if (key.isWritable() && key.isValid()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        if (sc.isConnected()) {
                            handleWrite(key);
                        } else {
                            System.out.println("is not connected!");
                        }
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (selector != null) {
                    selector.close();
                }
                if (ssc != null) {
                    ssc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        if (ssc != null) {
            try {
                ssc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("www", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }
}

