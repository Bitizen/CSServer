package com.bitizen.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class CounterSwipeServer extends Thread {

	private ArrayList<ConnectionRequestHandler> threads = new ArrayList<ConnectionRequestHandler>();
	final static int _portNumber = 5559;
 
	public static void main(String[] args) {
		try {
			new CounterSwipeServer().startServer();
		} catch (Exception e) {
			System.out.println("I/O failure: " + e.getMessage());
			e.printStackTrace();
		}
 
	}
 
	public void startServer() throws Exception {
		ServerSocket serverSocket = null;
		boolean listening = true;
 
		try {
			serverSocket = new ServerSocket(_portNumber);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + _portNumber);
			System.exit(-1);
		}
 
		while (listening) {
			handleClientRequest(serverSocket);
		}

		serverSocket.close();
	}
 
	private void handleClientRequest(ServerSocket serverSocket) {
		try {
			ConnectionRequestHandler aThread = new ConnectionRequestHandler(serverSocket.accept());
			threads.add(aThread);
			aThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}