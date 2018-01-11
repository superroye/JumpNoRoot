package com.wolf.jumpnoroot.javaclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;

//本类运行在pc端，用于接收手机端的server push的adb shell input swipe命令
public class JumpAdbClient {
	static Selector selector;

	public static void main(String[] args) {
		client("172.16.15.169", "8089");
	}

	public static void client(String host, String port) {
		SocketChannel socketChannel = null;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress(host, Integer.parseInt(port)));

			readSelector(socketChannel);
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				if (socketChannel != null) {
					socketChannel.close();
				}
				selector.close();
			} catch (IOException e1) {
				e.printStackTrace();
			}
		}
	}

	static void readSelector(SocketChannel ss) {
		try {
			selector = Selector.open();
			ss.register(selector, SelectionKey.OP_CONNECT);
			while (true) {
				selector.select();
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					if (key.isConnectable()) {
						System.out.println("=== client isConnectable");
						if (ss.isConnectionPending()) {
							if (ss.finishConnect()) {
								key.interestOps(SelectionKey.OP_READ);
								ByteBuffer buf = ByteBuffer.allocate(1024);
								key.attach(buf);
							} else {
								key.cancel();
							}
						}
					} else if (key.isReadable()) {
						handleRead(key);
					}
					iter.remove();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void handleWrite(SelectionKey key) throws IOException {
		System.out.println("=== handleWrite key");
		ByteBuffer buf = (ByteBuffer) key.attachment();
		buf.flip();
		SocketChannel sc = (SocketChannel) key.channel();
		while (buf.hasRemaining()) {
			sc.write(buf);
		}
		buf.compact();
	}

	public static void handleRead(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();
		buf.clear();
		sc.read(buf);
		buf.flip();

		sendCmd(getString(buf));
	}

	public static void sendCmd(String command) {
		if (command == null) {
			return;
		}
		Runtime run = Runtime.getRuntime();
		try {
			System.out.println("sendCmd: " + command);

			Process process = run.exec(command);
			InputStream in = process.getInputStream();
			while (in.read() != -1) {
				System.out.print(in.read());
			}
			System.out.println("\n");
			in.close();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getString(ByteBuffer buffer) {
		Charset charset = null;
		CharsetDecoder decoder = null;
		CharBuffer charBuffer = null;
		try {
			charset = Charset.forName("UTF-8");
			decoder = charset.newDecoder();
			// 用这个的话，只能输出来一次结果，第二次显示为空
			// charBuffer = decoder.decode(buffer);
			charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
			return charBuffer.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return "error";
		}
	}
}
