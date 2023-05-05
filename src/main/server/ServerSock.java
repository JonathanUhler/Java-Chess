// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// ServerSock.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package server;


import util.Log;
import util.CRC;
import client.ClientSock;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class ServerSock
//
// C-style server socket wrapper
//
public class ServerSock {

	private ServerSocket serverSocket;
	private BufferedReader IN;
	private PrintWriter OUT;


	// ====================================================================================================
	// public void bind
	//
	// Binds to a given ip/port and sets the listen backlog
	//
	// Arguments--
	//
	//  ip:      the ip address to bind to
	//
	//  port:    the port to bind to
	//
	//  backlog: the number of pending connections to hold onto in the queue
	//
	public void bind(String ip, int port, int backlog) {
		try {
			this.serverSocket = new ServerSocket(port, backlog, InetAddress.getByName(ip));
		}
		catch (IOException e) {
			Log.stdlog(Log.ERROR, "ServerSock", "IOException thrown when initializing socket");
			Log.stdlog(Log.ERROR, "ServerSock", "\t" + e);
		}
	}
	// end: public void bind


	// ====================================================================================================
	// public Socket accept
	//
	// C-style accept method
	//
	// Returns--
	//
	//  A Socket object if the connection was successful, otherwise null
	//
	public Socket accept() {
		try {
			if (this.serverSocket != null)
				return this.serverSocket.accept();
		}
		catch (IOException e) {
			Log.stdlog(Log.ERROR, "ServerSock", "IOException thrown on accept call, returning null");
			return null;
		}
		
		Log.stdlog(Log.ERROR, "ServerSock", "no socket returned in accept(), serverSocket might be null");
		return null;
	}
	// end: public Socket accept


	// ====================================================================================================
	// public void send
	//
	// C-style send method
	//
	// Arguments--
	//
	//  message:          the message to send
	//
	//  clientConnection: the client to send to
	public void send(String message, ClientSock clientConnection) {
		PrintWriter OUT = new PrintWriter(clientConnection.getOutputStream(), true); // true for autoFlush
		message += CRC.get(message); // Add checksum
		OUT.println(message);
	}
	// end: public void send


	// ====================================================================================================
	// public String recv
	//
	// C-style recv method
	//
	// Arguments--
	//
	//  clientConnection: the client to receive from
	//
	// Returns--
	//
	//  The received message if successful, otherwise null
	//
	public String recv(ClientSock clientConnection) {
		try {
			BufferedReader IN = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
			String recv = IN.readLine(); // Convert from char array to string

			if (recv != null) {
				boolean crcValid = CRC.check(recv); // Check checksum
				if (!crcValid) {
					Log.stdlog(Log.ERROR, "ServerSock", "CRC check failed, disconnecting client");
					return null;
				}
				return recv.substring(0, recv.length() - CRC.SIZE);
			}

			Log.stdlog(Log.WARN, "ServerSock", "Received \"null\", client probably disconnected");
			return null;
		}
		catch (IOException e) {
			Log.stdlog(Log.WARN, "ServerSock", "IOException thrown from readLine(), client probably disconnected");
			Log.stdlog(Log.WARN, "ServerSock", "\t" + e);
			return null;
		}
	}
	// end: public String recv
	
}
// end: public class ServerSock