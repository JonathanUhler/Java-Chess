package client;


import jnet.JClientSocket;
import server.Server;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.io.IOException;


public class GameView extends View {

	/** The client used for network communication. */
	private JClientSocket client;
	/** The server hosted by this client, {@code null} is not applicable. */
	private Server server;
	

    public GameView(Screen owner) {
		super(owner);

		this.client = null;
		this.server = null;
		
		super.redraw();
	}


	public void startServer(String ip, int port) {
		if (this.server != null) {
			Chess.displayMessage("Server Error", "You have already started a server.\n" +
								 "Please quit this application and reopen to start a new server.");
			return;
		}

		Thread serverThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Server server = new Server(ip, port);
					}
					catch (IOException e) {
						Chess.displayMessage("Connection Error", "Unable to start server on " +
											 ip + ":" + port + ".\n" + e);
						Thread.currentThread().interrupt();
						return;
					}
				}
			});
		serverThread.start();

		// Small delay to make sure the server is up before attempting a connection
		try {
			Thread.sleep(500);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	public void connect(String ip, int port, boolean hosting) {
		if (hosting)
			this.startServer(ip, port);
		
		this.client = new JClientSocket();

		try {
			this.client.connect(ip, port);
		}
		catch (IOException e) {
			this.client = null;
			super.owner().displayMainView();
			Chess.displayMessage("Connection Error", "Unable to connect to server at " +
								 ip + ":" + port + ".\n" + e);
			return;
		}

		Thread listenThread = new Thread(this::listen);
		listenThread.start();
	}


	private void listen() {
		while (true) {
			String recv = this.client.srecv();
			if (recv == null)
				break;

			/*
			System.out.println("Message from server: " + recv);
			this.label.setText("Test message from server: " + recv);
			this.client.send("Echo from " + this.client.toString() + ": " + recv);
			*/

			/*
			// TEST CODE
			try {
				this.board = new Board("/Users/jonathan/Documents/Computer-Science/Java/Java-Risk/src/assets/maps/world.map");
			}
			catch (IOException e) {
				this.client.close();
				super.owner().displayMainView();
				Client.displayMessage("File Error", "Cannot read map file.\n" + e);
				return;
			}
			*/
			super.redraw();
		}
	}


	@Override
	public void display() {
		/*
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		this.add(this.label, gbc);
		*/
		/*
		if (this.board != null) {
			this.board.getView().setPreferredSize(new Dimension(Screen.BOARD_WIDTH, Screen.HEIGHT));
			this.add(this.board.getView(), gbc);
		}
		*/
	}

}
