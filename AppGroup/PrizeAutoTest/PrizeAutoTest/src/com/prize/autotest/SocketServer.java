package com.prize.autotest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import android.net.Credentials;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.os.Message;

public class SocketServer implements Runnable {
	private LocalServerSocket server;
	private LocalSocket client;
	private OutputStream os;
	private BufferedReader is;
	private Handler handler;

	public SocketServer(Handler handler) {
		this.handler = handler;
	}

	public void send(byte[] data) {
		if (os != null) {
			try {
				os.write(data);
				os.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		try {
			server = new LocalServerSocket("autotest");
			waitConnect();
			Credentials cre = client.getPeerCredentials();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int result = 0;
		while (true) {
			try {
				if (is == null) {
					break;
				}
				char[] buffer = new char[4];
				result = is.read(buffer);
				if (result == -1) {
					if (client != null) {
						closeClient();
						waitConnect();
					}
				} else {
					send(("1").getBytes());
					sendMessageLoop(buffer);
				}
				Thread.sleep(100);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void waitConnect() {
		try {
			client = server.accept();
			os = client.getOutputStream();
			is = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMessageLoop(char[] buffer) {
		Message msg = handler.obtainMessage();
		msg.obj = String.valueOf(buffer).trim();
		handler.sendMessage(msg);
	}

	private void closeClient() {
		try {
			if (os != null) {
				os.close();
			}
			if (is != null) {
				is.close();
			}
			if (client != null) {
				client.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			if (os != null) {
				os.close();
			}
			if (is != null) {
				is.close();
			}
			if (client != null) {
				client.close();
			}
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}