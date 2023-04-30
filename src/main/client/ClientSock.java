// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// ClientSock.java
// Networking-Chess
//
// Create by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package client;


import util.Log;
import util.CRC;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class ClientSock
//
// C-style socket wrapper for clients
//
public class ClientSock {

	private Socket clientSocket;
	private BufferedReader IN;
	private PrintWriter OUT;


	// ----------------------------------------------------------------------------------------------------
	// public ClientSock
	//
	// Allows construction of a ClientSock object as usual. Using the second constructor, an existing
	// java Socket object can be provided instead
	//
	public ClientSock() {}
	// end: public ClientSock


	// ----------------------------------------------------------------------------------------------------
	// public ClientSock
	//
	// Arguments--
	//
	//  clientSock: a java Socket object used to begin the initialization of a ClientSock object
	//
	public ClientSock(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	// end: public ClientSock
	

	// ====================================================================================================
	// public InputStream getInputStream
	//
	// Returns the InputStream object from the locally stored java Socket object. This is provided so
	// servers can "listen" and "send" on the client socket. This function, unlike the C methods, is done
	// in java through the I/O streams, which needs to be taken from the client socket for the listen/send
	// calls
	//
	// Returns--
	//
	//  An InputStream object connected to the internal Socket object
	//
	public InputStream getInputStream() {
		if (this.clientSocket != null) {
			try {
				return this.clientSocket.getInputStream();
			}
			catch (IOException e) {
				Log.stdlog(Log.ERROR, "ClientSock", "IOException thrown by getInputStream, returning null");
				Log.stdlog(Log.ERROR, "ClientSock", "\t" + e);
				return null;
			}
		}
		return null;
	}
	// end: public InputStream getInputStream


	// ====================================================================================================
	// public OutputStream getOutputStream
	//
	// Returns the OutputStream object from the local Socket object, similarly to the getInputStream method
	//
	// Returns--
	//
	//  An OutputStream object connected to the internal Socket object
	public OutputStream getOutputStream() {
		if (this.clientSocket != null) {
			try {
				return this.clientSocket.getOutputStream();
			}
			catch (IOException e) {
				Log.stdlog(Log.ERROR, "ClientSock", "IOException thrown by getOutputStream, returning null");
				Log.stdlog(Log.ERROR, "ClientSock", "\t" + e);
				return null;
			}
		}
		return null;
	}
	// end: public OutputStream getOutputStream


	// ====================================================================================================
	// public void connect
	//
	// Performs the action of the C socket connect() function, and wraps the creation of the java
	// I/O streams for communication
	//
	// Arguments--
	//
	//  ip:   an IP address to connect to
	//
	//  port: a port to connect to
	public void connect(String ip, int port) {
		try {
			// Because a constructor is provided to take in an existing socket object, a null check is put
			// here to confirm the socket needs to be created. Otherwise, it is assumed the passed socket
			// contains the correct IP/port. Although, this method is not actually called in the case
			// where the second constructor is used
			if (this.clientSocket == null)
				this.clientSocket = new Socket(ip, port);

			// This is java's way of handling socket I/O. The main purpose of this method is to wrap
			// the creation of these stream objects to make for a cleaner implementation
			this.IN = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			this.OUT = new PrintWriter(this.clientSocket.getOutputStream(), true); // true for autoFlush
		}
		catch (IOException e) {
			Log.stdlog(Log.ERROR, "ClientSock", "IOException thrown when initializing socket");
			Log.stdlog(Log.ERROR, "ClientSock", "\t" + e);
			Log.gfxmsg("Network Error", "Client failed to connect");
		}
	}
	// end: public void connect


	// ====================================================================================================
	// public void send
	//
	// C-style send method
	//
	// Arguments--
	//
	//  msg: the message to send
	//
	public void send(String msg) {		
		if (this.OUT != null) {
			msg += CRC.get(msg); // Add checksum
			this.OUT.println(msg);
			return;
		}
		Log.stdlog(Log.ERROR, "ClientSock", "OUT was null, no message sent");
	}
	// end: public void send


	// ====================================================================================================
	// public String recv
	//
	// C-style receive method
	//
	// Returns--
	//
	//  A string if one was received, or null upon error
	//
	public String recv() {
		if (this.IN != null) {
			try {
				String recv = this.IN.readLine();

				if (recv != null) {
					boolean crcValid = CRC.check(recv); // Check checksum
					if (!crcValid) {
						Log.stdlog(Log.ERROR, "ClientSock", "CRC check failed");
						Log.gfxmsg("Network Error", "Received malformed data (CRC check failed)");
						return null;
					}
					return recv.substring(0, recv.length() - CRC.SIZE);
				}

				Log.stdlog(Log.WARN, "ClientSock", "Received \"null\", server probably closed");
				return null;
			}
			catch (IOException e) {
				Log.stdlog(Log.ERROR, "ClientSock", "IOException thrown from IN.readLine(), returning null");
				return null;
			}
		}
		Log.stdlog(Log.ERROR, "ClientSock", "IN was null, returning null");
		return null;
	}
	// end: public String recv


	// ====================================================================================================
	// public void close
	//
	// C-style close method
	//
	public void close() {
		if (this.clientSocket != null) {
			try {
				this.clientSocket.close();
			}
			catch (IOException e) {
				Log.stdlog(Log.ERROR, "ClientSock", "clientSocket could not be closed");
				Log.stdlog(Log.ERROR, "ClientSock", "\t" + e);
			}
		}
	}
	// end: public void close
	
}
// end: public class ClientSock
