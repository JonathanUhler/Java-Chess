import java.net.Socket;
import java.net.UnknownHostException;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import client.ClientSock;
import java.util.Scanner;


public class Client {

	public static void main(String[] args) {
		ClientSock cs = new ClientSock();
		cs.connect("127.0.0.1", 9000);

		Scanner stdin = new Scanner(System.in);
		while (true) {
			String msg = stdin.nextLine(); // (3) .send()
			if (!msg.equals(""))
				cs.send(msg);
			
			String recv = cs.recv(); // (4) .recv()
			System.out.println("RESPONSE: \"" + recv +  "\"");
		}
		
		/*
		try {
			Socket socket = new Socket("127.0.0.1", 9000); // (1) new Socket(), and (2) .connect()

			BufferedReader IN = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter OUT = new PrintWriter(socket.getOutputStream(), true); // true for autoFlush

			Scanner stdin = new Scanner(System.in);
			while (true) {
			    String msg = stdin.nextLine(); // (3) .send()
				if (!msg.equals(""))
					OUT.println(msg);

				String recv = IN.readLine(); // (4) .recv()
				System.out.println("RESPONSE: \"" + recv +  "\"");
			}
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		*/
	}
	
}
