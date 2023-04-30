import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class Server {

	public static void main(String[] args) {
		try {
			// (1) new Socket(), and (2) .bind(), and (3) .listen()
			ServerSocket serverSocket = new ServerSocket(5000);

			Socket clientConnection = serverSocket.accept(); // (4) .accept()

			BufferedReader IN = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
			PrintWriter OUT = new PrintWriter(clientConnection.getOutputStream(), true); // true for autoFlush

			while (true) {
				Object recv = IN.readLine(); // (5) .recv()
				Obj recvObj = (Obj) recv;
				System.out.println("local to server, received Obj object: " + recvObj);
				OUT.println("echo from server |" + recv + "|"); // (6) .send()
			}
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
}
