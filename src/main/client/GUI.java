// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// GUI.java
// Networking-Chess
//
// Create by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package client;


import util.Log;
import util.StringUtility;
import util.Coordinate;
import server.Server;
import engine.board.BoardInfo;
import engine.fen.FenUtility;
import engine.piece.Piece;
import engine.move.Move;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class GUI extends JPanel
//
// GUI and network manager for the client
//
// Notable threads--
//
// public void hostServer(String, String) --> serverThread:    used to run an instance of the Server
//                                                             class when a client chooses to host a
//                                                             server, so that the client can still
//                                                             function as normal and play in a
//                                                             separate thread
//
// public void connectClient(String, String) --> listenThread: used to run the GUI.listen() method which
//                                                             listens for incoming messages from the
//                                                             server
//
public class GUI extends JPanel {

	// Margins and sizes
	public static final int MARGIN = 40;
	public static final int TILE_SIZE = 70;

	private ArrayList<Coordinate> highlightedTiles;
	private PiecePane piecePane;
	
	private BoardInfo boardInfo;
	private ClientSock clientSocket;
	private int color;


	// ----------------------------------------------------------------------------------------------------
	// public GUI
	//
	public GUI() {
		this.setPreferredSize(new Dimension(GUI.MARGIN + (GUI.TILE_SIZE * 8) + GUI.MARGIN,
											GUI.MARGIN + (GUI.TILE_SIZE * 8) + GUI.MARGIN));
		
		JTextField ipTextField = new JTextField(10);
		JTextField portTextField = new JTextField(4);
		JButton hostButton = new JButton("Host Game");
		hostButton.addActionListener(e -> this.hostServer(ipTextField.getText(), portTextField.getText()));
		JButton joinButton = new JButton("Join Game");
		joinButton.addActionListener(e -> this.connectClient(ipTextField.getText(), portTextField.getText()));

		this.add(new JLabel("IP Addr:"));
		this.add(ipTextField);
		this.add(new JLabel("Port:"));
		this.add(portTextField);
		this.add(hostButton);
		this.add(joinButton);

		this.highlightedTiles = new ArrayList<>();
		// Piece images, as JLabels, are contained within a PiecePane object (JLayeredPane) to make redrawing
		// and layering easier
		this.piecePane = new PiecePane(this);
		this.piecePane.setBounds(GUI.MARGIN, GUI.MARGIN, GUI.TILE_SIZE * 8, GUI.TILE_SIZE * 8);
		this.add(this.piecePane);

		this.color = Piece.Color.NONE;
		this.updatePosition("8/8/8/8/8/8/8/8 w - - 0 1");
	}
	// end: public GUI


	// ====================================================================================================
	// GET methods
	public int getColor() {
		return this.color;
	}

	public BoardInfo getBoardInfo() {
		return this.boardInfo;
	}
	// end: GET methods


	// ====================================================================================================
	// SET methods
	public void setHighlightedTiles(ArrayList<Coordinate> highlightedTiles) {
		this.highlightedTiles = highlightedTiles;
		this.repaint();
	}
	// end: SET methods


	// ====================================================================================================
	// private void hostServer
	//
	// Creates a server from the client GUI, preventing the need to open a second terminal window and start
	// the server from there, although that can still be done if desired
	//
	// Arguments--
	//
	//  ip:         the IP address to run the server on
	//
	//  stringPort: a string representing the port number to run the server on
	private void hostServer(String ip, String stringPort) {
		// Parse the port as an integer
		int port;
		try {
			port = Integer.parseInt(stringPort);
		}
		catch (NumberFormatException e) {
			Log.stdlog(Log.ERROR, "GUI", "could not parse port to start server");
			Log.gfxmsg("Parse Error", "Cannot start server: port cannot be parsed as an integer");
			return;
		}

		// Create and start a new thread to host the server, allowing the client to play with the server
		// running in parallel
		Thread serverThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Server server = new Server(ip, port);
				}
			});
		serverThread.start();

		// Small delay to make sure the server is up before attempting a connection
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Connect the client
		this.connectClient(ip, stringPort);
	}
	// end: private void hostServer


	// ====================================================================================================
	// private void connectClient
	//
	// Connects a client to a server running on the given ip and port
	//
	// Arguments--
	//
	//  ip:         the IP address to connect to
	//
	//  stringPort: a string representing the port to connect to
	private void connectClient(String ip, String stringPort) {
		// Parse the port as an integer
		int port;
		try {
			port = Integer.parseInt(stringPort);
		}
		catch (NumberFormatException e) {
			Log.stdlog(Log.ERROR, "GUI", "could not parse port to start client");
			Log.gfxmsg("Parse Error", "Cannot connect: port cannot be parsed as an integer");
			return;
		}

		// Create the client
		this.clientSocket = new ClientSock();
		this.clientSocket.connect(ip, port);

		// Initial handshake to get the current board state and assigned color for this client.
		//  - Recover handshake information from server and check for null
		//  - Parse this string as a map and check for null and proper keys
		//  - Update the client's information
		String recv = this.clientSocket.recv();
		if (recv == null) {
			Log.stdlog(Log.ERROR, "GUI", "initial handshake failed");
			Log.gfxmsg("Network Error", "Unable to establish connection with server (failed handshake)");
			return;
		}
		
		Map<String, String> mapRecv = StringUtility.stringToMap(recv);
		if (mapRecv == null || !mapRecv.containsKey("color") || !mapRecv.containsKey("fen")) {
			Log.stdlog(Log.ERROR, "GUI", "connectClient: could not process server message (not map or invalid keys)");
			Log.stdlog(Log.ERROR, "GUI", "\tmap: " + mapRecv);
			Log.stdlog(Log.ERROR, "GUI", "\trecv str: " + recv);
			Log.gfxmsg("Network Error", "Received malformed data from server");
			return;
		}
		
		this.color = Integer.parseInt(mapRecv.get("color"));
		this.updatePosition(mapRecv.get("fen"));

		// Start the client listening process on a second thread
		Thread listenThread = new Thread(this::listen);
		listenThread.start();
	}
	// end: private void connectClient


	// ====================================================================================================
	// private void listen
	//
	// Continuously pulls on the connected socket for board information sent from the server
	//
	private void listen() {
		while (true) {
			String recv = this.clientSocket.recv();
			if (recv == null)
				continue;

			Map<String, String> mapRecv = StringUtility.stringToMap(recv);
			if (mapRecv == null || !mapRecv.containsKey("fen") || !mapRecv.containsKey("state")) {
				Log.stdlog(Log.ERROR, "GUI", "listen: could not process server message (not map or invalid keys)");
				Log.stdlog(Log.ERROR, "GUI", "\tmap: " + mapRecv);
				Log.stdlog(Log.ERROR, "GUI", "\trecv str: " + recv);
				Log.gfxmsg("Network Error", "Received malformed data from server (map error)");
				continue;
			}

			// Update the board position for this client and check the state of the game. If the game is not
			// ongoing, show a message to the client with information about how the game ended
			String fenString = mapRecv.get("fen");
			String state = mapRecv.get("state");
			this.updatePosition(fenString);

			switch (state) {
			case "1":
				Log.gfxmsg("Game Finished", "White wins by checkmate");
				break;
			case "2":
				Log.gfxmsg("Game Finished", "Black wins by checkmate");
				break;
			case "3":
				Log.gfxmsg("Game Finished", "Draw by stalemate");
				break;
			case "4":
				Log.gfxmsg("Game Finished", "Draw by fifty-move rule");
				break;
			case "5":
				Log.gfxmsg("Game Finished", "Draw by threefold repetition");
				break;
			case "6":
				Log.gfxmsg("Game Finished", "Draw by insufficient material");
				break;
			}
		}
	}
	// end: private void listen


	// ====================================================================================================
	// public void send
	//
	// Sends a move for this client to the server based on a start and end tile for the move
	//
	// Arguments--
	//
	//  startTile: the tile the piece moved started on
	//
	//  endTile:   the tile the piece moved ended on
	//
	public void send(Coordinate startTile, Coordinate endTile) {
		Map<String, String> moveMap = new HashMap<String, String>();
		moveMap.put("color", Integer.toString(this.color));
		moveMap.put("startTile", StringUtility.coordinateToString(startTile));
		moveMap.put("endTile", StringUtility.coordinateToString(endTile));
		moveMap.put("flag", Integer.toString(Move.Flag.inferFlag(this.boardInfo.getPiece(startTile),
																 startTile, endTile,
																 this.boardInfo.enPassantTile)));
		
		String move = StringUtility.mapToString(moveMap);
		if (move == null) {
			Log.stdlog(Log.ERROR, "GUI", "could not process message to send as map");
			Log.gfxmsg("Parse Error", "Could not send move to server, try again");
			return;
		}
		
		this.clientSocket.send(move);		
	}
	// end: public void send


	// ====================================================================================================
	// private void updatePosition
	//
	// Updates the PiecePane object and BoardInfo object for this client
	//
	// Arguments--
	//
	//  fenString: a new FEN string representing the position
	private void updatePosition(String fenString) {
		this.boardInfo = FenUtility.informationFromFen(fenString);
		this.piecePane.drawPosition();
		this.repaint();
	}
	// end: private void updatePosition
	

	// ====================================================================================================
	// public void paintComponent
	//
	// Draws still components of the game, such as the board tiles and current fen position
	//
	// Arguments--
	//
	//  g: a Graphics object to draw with
	//
	@Override
	public void paintComponent(Graphics g) {
		// Draw the FEN string
		g.drawString("Position: " + this.boardInfo.fenString, 50,
					 GUI.MARGIN + (GUI.TILE_SIZE * 8) + (int)(GUI.MARGIN / 1.5));

		// Draw each of the tiles of the board. For every other tile (shifting by 1 each row), change the color.
		// Highlight legal moves in red for the piece picked up, otherwise draw tiles with a tan/wood theme
		for (Coordinate c : Coordinate.getAllValidCoordinates()) {
			if ((c.getX() + c.getY()) % 2 == 0) {
				// Dark tiles
				if (this.highlightedTiles.contains(c))
					g.setColor(new Color(161, 86, 86));
				else
					g.setColor(new Color(175, 137, 104));
			}
			else {
				// Light tiles
				if (this.highlightedTiles.contains(c))
					g.setColor(new Color(238, 156, 156));
				else
					g.setColor(new Color(236, 217, 185));
			}

			int displayOffsetX = (this.color != Piece.Color.BLACK) ? (c.getX()) : (7 - c.getX());
			int displayOffsetY = (this.color != Piece.Color.BLACK) ? (7 - c.getY()) : (c.getY());
			g.fillRect(GUI.MARGIN + (displayOffsetX * GUI.TILE_SIZE),
					   GUI.MARGIN + (displayOffsetY * GUI.TILE_SIZE),
					   GUI.TILE_SIZE, GUI.TILE_SIZE);
		}
	}
	// end: public void paintComponent

}
// end: public class GUI