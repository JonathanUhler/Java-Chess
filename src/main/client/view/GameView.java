package client.view;


import jnet.JClientSocket;
import jnet.JServer;
import client.Client;
import client.Screen;
import client.component.PiecePane;
import server.Communication;
import java.io.IOException;


/**
 * Extendable graphical view for a chess variant. This abstract class provides a framework
 * for network communication (by maintaining a client socket and optional server that the
 * client can self-host) as well as the general {@code PiecePane} graphical component.
 * <p>
 * Any child class is expected to initialize server hosting (through the {@code startServer}
 * and {@code setServer} methods) and communication. Children are also responsible for drawing the 
 * graphical context by overriding the {@code display} method of the {@code View} super-class.
 *
 * @see startServer
 * @see setServer
 * @see client.view.View
 *
 * @author Jonathan Uhler
 */
public abstract class GameView extends View {

	/** The client used for network communication. */
	private JClientSocket client;
	/** The server hosted by this client, {@code null} is not applicable. */
	private JServer server;

	/** Main graphical component (contains the board and pieces, responsible for moves). */
	private PiecePane piecePane;
	

	/**
	 * Constructs a new {@code GameView} object. This object manages a traditional 2-player
	 * chess game without any modifications. To create additional chess variants, write
	 * a new {@code View}.
	 *
	 * @param owner  a {@code Screen} object that manages this view.
	 */
    public GameView(Screen owner) {
		super(owner);

		this.client = null;
		this.server = null;
		this.piecePane = null;
		
		super.redraw();
	}


	/**
	 * Returns the client socket object used for networking communication.
	 *
	 * @return the client socket object.
	 */
	public JClientSocket getClientSocket() {
		return this.client;
	}


	/**
	 * Returns the server, if any, hosted by this client. If not server is currentl hosted
	 * by this client, {@code null} is returned.
	 *
	 * @return the server hosted by this client.
	 */
	public JServer getServer() {
		return this.server;
	}


	/**
	 * Returns the pane of the chess board seen by this client. The value returned by this
	 * method will be {@code null} until {@code setPiecePane} is called with a non-{@code null}
	 * value. It is recommended that any child class checks for {@code null} before attempting
	 * to add the piece pane in the {@code display} routine.
	 * <p>
	 * This class does not handle any event from the {@code PieceAdapter} associated with the
	 * pane. The child class is responsible for adding an appropriate action listener
	 * and action method to the piece pane in order to receive events from the adapter.
	 * A recommended implementation of such a listener is as follows:
	 * <pre>
	 * {@code
	 * PiecePane piecePane = super.getPiecePane();
	 * if (piecePane == null)
	 *     return;
	 * piecePane.addActionListener(e -> this.actionPerformed(e));
	 * }
	 * </pre>
	 * Refer to the documentation of {@code PiecePane} for more information on the
	 * {@code ActionEvent}s generated.
	 *
	 * @return the pane of the chess board seen by this client.
	 *
	 * @see setPiecePane
	 * @see client.component.PiecePane
	 */
	public PiecePane getPiecePane() {
		return this.piecePane;
	}


	/**
	 * Sets the server managed by this client. This method should be invoked as part of a
	 * child class's implementation of the {@code startServer} routine.
	 *
	 * @param server  the server managed by this client.
	 */
	public void setServer(JServer server) {
		this.server = server;
	}


	/**
	 * Sets the piece pane managed seen by this client.
	 *
	 * @param piecePane  the piece pane seen by this client.
	 */
	public void setPiecePane(PiecePane piecePane) {
		this.piecePane = piecePane;
	}


	/**
	 * Connects the client socket managed by this view to a server. This method allows
	 * the client to optionally host its own server through the {@code hosting} parameter.
	 * <p>
	 * If specified by {@code hosting == true}, this client starts a server by invoking the
	 * {@code startServer} abstract method. This method is abstract because any child class
	 * of the {@code jnet.JServer} class may be used as a valid server. It is up to the
	 * child of this {@code GameView} to choose a specific type of server to run.
	 *
	 * @param ip       the IP address of the server to join.
	 * @param port     the port of the server to join.
	 * @param hosting  whether this client should start its own server before joining that server.
	 */
	public void connect(String ip, int port, boolean hosting) {
		// Host a new server, if specified
		if (hosting) {
			this.startServer(ip, port);

			// Small delay to make sure the server is up before attempting a connection
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Initialize the client to connect to the server
		this.client = new JClientSocket();

		try {
			this.client.connect(ip, port);
		}
		catch (IOException e) {
			this.client = null;
			super.owner().displayMainView();
			Client.displayMessage("Connection Error", "Unable to connect to server at " +
								 ip + ":" + port + ".\n" + e);
			return;
		}

		Thread listenThread = new Thread(this::listen);
		listenThread.start();
	}


	/**
	 * Listens on the client socket for incoming communications from a server. This method
	 * invokes the abstract {@code serverCommunicated} method, then redraws the
	 * graphical context of this {@code View}.
	 */
	private void listen() {
		while (true) {
			byte[] recv = this.client.recv();
			if (recv == null)
				break;

			this.serverCommunicated(recv);
			super.redraw();
		}
	}


	/**
	 * Hosts a new server on the specified IP address and port. This routine should call the
	 * {@code setServer} method as part of the server initialization. This method is invoked
	 * by the {@code MainView} of this chess application after the end-user chooses
	 * "Host Game" and enters an IP/port combination.
	 * <p>
	 * The argument IP address and port may or may not be valid, and may or may not already
	 * be bound to by another server. The child of this {@code GameView} is responsible
	 * for catching any errors that result from {@code JServer} initialization.
	 * <p>
	 * Because the {@code JServer} constructor is blocking in the scope of the current thread,
	 * it is <b>highly</b> advised that the implementation of this method creates a new thread
	 * which runs the server and calls {@code setServer}.
	 * <p>
	 * An example implementation of this method, which creates some arbitrary {@code Server}
	 * object that is a child of {@code JServer}, is as follows:
	 * <pre>
	 * {@code 
	 * @Override
	 * public void startServer(String ip, int port) {
	 *     Thread serverThread = new Thread(new Runnable() {
	 *         @Override
	 *         public void run() {
	 *             try {
	 *                 MyView.super.setServer(new Server(ip, port));
	 *             }
	 *             catch (IOException e) {
	 *                 // handle error
	 *             }
	 *         }
	 *     });
	 *     serverThread.start();
	 * }
	 * }
	 * </pre>
	 *
	 * @param ip    the IP address to host the server on.
	 * @param port  the port to host the server on.
	 *
	 * @see setServer
	 */
	public abstract void startServer(String ip, int port);


	/**
	 * Invoked when a message is received from a server on this view's client socket. This
	 * method is called immediately before a call to the {@code redraw} method of the
	 * {@code View} super-class.
	 *
	 * @param recv  the byte array received from the server.
	 */
	public abstract void serverCommunicated(byte[] recv);

}
