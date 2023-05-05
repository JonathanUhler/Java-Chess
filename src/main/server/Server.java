// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Server.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package server;


import util.Log;
import util.StringUtility;
import util.Coordinate;
import tests.PerftTest;
import client.ClientSock;
import engine.board.Board;
import engine.board.BoardInfo;
import engine.fen.FenUtility;
import engine.move.Move;
import engine.move.MoveGenerator;
import engine.piece.Piece;
import java.net.Socket;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Server
//
// Main class to handle client communication and internal game state. Supports a simple CLI to allow
// for easy testing and server-side manipulation of the board
//
// Notable threads--
//
//  public Server(String, int) --> acceptThread: used to run the Server.accept() method so the CLI
//                                               can run at the same time
//
//  private void add(Socket) --> clientThread:   runs the Server.listenOnClient(ClientSock) method
//                                               so the Server.accept() method can run at the same
//                                               time
//
public class Server {

	// Note: this is not the actual address of the server as seen by the client. From there, you can
	//       host a server at any valid IP/port. This variables are for the default constructor to
	//       make debug constructing easier
	public static final String IP_ADDR = "127.0.0.1";
	public static final int PORT = 9000;
	

	private ArrayList<ClientSock> clientConnections;
	private ServerSock serverSocket;
	private String ip;
	private int port;
	private ServerCLI cli;

	private Board board;


	// ----------------------------------------------------------------------------------------------------
	// public Server
	//
	public Server() {
		this(Server.IP_ADDR, Server.PORT);
	}
	// end: public Server


	// ----------------------------------------------------------------------------------------------------
	// public Server
	//
	// Arguments--
	//
	//  ip:   the ip to start the server on
	//
	//  port: the port to start the server on
	//
	public Server(String ip, int port) {
		this.ip = ip;
		this.port = port;
		
		this.clientConnections = new ArrayList<>();
		
		this.serverSocket = new ServerSock();
		this.serverSocket.bind(this.ip, this.port, 50);

		BoardInfo boardInfo = FenUtility.informationFromFen(Board.START_FEN);
		this.board = new Board(boardInfo);

		// Start the accept method in a new thread so the scanner command line interface can work at the
		// same time
		Thread acceptThread = new Thread(() -> this.accept());
		acceptThread.start();

		// Command line interface
		this.cli = new ServerCLI(this);
		this.cli.run();
	}
	// end: public Server


	// ====================================================================================================
	// CLI methods
	//
	public void setBoardInfo(BoardInfo boardInfo, ServerCLI sender) {
		if (!sender.equals(this.cli)) {
			Log.stdlog(Log.WARN, "Server", "setBoardInfo called by illegal sender, ignoring");
			return;
		}
		
		if (boardInfo == null)
			Log.stdlog(Log.ERROR, "Server", "CLI: setBoardInfo called with null boardInfo");
		this.board = new Board(boardInfo);
		this.sendAll(StringUtility.mapToString(this.getReturnData()));
	}

	public BoardInfo getBoardInfo(ServerCLI sender) {
		if (!sender.equals(this.cli)) {
			Log.stdlog(Log.WARN, "Server", "getBoardInfo called by illegal sender, ignoring");
			return null;
		}

		return this.board.getInfo();
	}

	public String getIP() {
		return this.ip;
	}

	public int getPort() {
		return this.port;
	}
	// end: CLI methods


	// ====================================================================================================
	// private String evaluate
	//
	// Evaluate a move received from a client and play it on the board if it is legal. Moves and responses
	// are in the following format:
	//
	//	Incoming message
	//   - {"color": "_", "startTile": "__", "endTile": "__", "flag": "__"}
	//   - Ex -- {"color": "1", "startTile": "a1", "endTile": "a2", "flag": "0"}
	//  Outgoing message
	//   - {"fen": "__________", "state": "_"}
	//   - Ex -- {"fen": "8/8/8/8/8/8/4n3/R3K2R w KQkq - 0 1", "state": "0"}
	//     State flag is for the state of the game (ongoing, white wins, black wins, draw, etc.) found
	//     in the Board.State class
	//
	// Arguments--
	//
	//  moveString: a string representation of the move
	//
	// Returns--
	//
	//  A response message to send to all clients with the board information after the move has been
	//  made
	//
	private String evaluate(String moveString) {
		// Parse the input string as a hashmap and prepare a hashmap to return
		Map<String, String> moveData = StringUtility.stringToMap(moveString);
		Map<String, String> returnData = this.getReturnData();

		// Check for all proper keys
		if (moveData == null ||
			!moveData.containsKey("color") ||
			!moveData.containsKey("startTile") ||
			!moveData.containsKey("endTile") ||
			!moveData.containsKey("flag")) {
			Log.stdlog(Log.ERROR, "Server", "client sent invalid move data. Did not contain key or was null");
			Log.stdlog(Log.ERROR, "Server", "\t" + moveString);
			return StringUtility.mapToString(returnData);
		}

		// Parse the keys from strings to more useful objects
		String colorString = moveData.get("color");
		String startTileString = moveData.get("startTile");
		String endTileString = moveData.get("endTile");
		String flagString = moveData.get("flag");

		int flag;
		int color;
		try {
			flag = Integer.parseInt(flagString);
			color = Integer.parseInt(colorString);
		}
		catch (NumberFormatException e) {
			Log.stdlog(Log.ERROR, "Server", "flag or color received is not a number");
			Log.stdlog(Log.ERROR, "Server", "\tflag=" + flagString + ", color=" + colorString);
			return StringUtility.mapToString(returnData);
		}

		// Check that the move is for the correct player
		if ((this.board.getInfo().whiteToMove && color == Piece.Color.BLACK) ||
			(!this.board.getInfo().whiteToMove && color == Piece.Color.WHITE)) {
			Log.stdlog(Log.DEBUG, "Server", "received move with opposite color, ignoring");
			return StringUtility.mapToString(returnData);
		}

		// Parse start and end tiles
		Coordinate startTile = StringUtility.stringToCoordinate(startTileString);
		Coordinate endTile = StringUtility.stringToCoordinate(endTileString);

		if (startTile == null ||
			endTile == null) {
			Log.stdlog(Log.WARN, "Server", "start or end tile could not be parsed");
			Log.stdlog(Log.WARN, "Server", "\tstartTile=" + startTileString + ", endTile=" + endTileString);
			return StringUtility.mapToString(returnData);
		}

		// Process the Move object
		Move move = new Move(startTile, endTile, flag);
		ArrayList<Move> legalMoves = MoveGenerator.generateLegalMoves(this.board.getInfo());
		
		if (!legalMoves.contains(move)) {
			Log.stdlog(Log.DEBUG, "Server", "illegal move attempted, ignoring");
			return StringUtility.mapToString(returnData);
		}

		this.board.makeMove(move);

		int boardState = this.board.getInfo().inferState();
		returnData = this.getReturnData();

		// Reset the game if the previous game finished
		if (boardState != 0) {
			BoardInfo boardInfo = FenUtility.informationFromFen(Board.START_FEN);
			this.board = new Board(boardInfo);
		}
		return StringUtility.mapToString(returnData);
	}
	// end: private String evaulate


	// ====================================================================================================
	// private void accept
	//
	// Accepts and adds incoming client connections
	//
	private void accept() {
		// Unconditionally wait for and accept client connections, then assign them an id/place in the array and
		// create a ClientSock object to represent that conneciton and allow .send() calls towards that client
		while (true) {
			Log.stdout(Log.INFO, "Server", "accept :: ready to handle incoming connection");
			Socket clientConnection = this.serverSocket.accept();

			this.add(clientConnection);
		}
	}
	// end: private void accept


	// ====================================================================================================
	// private void listenOnClient
	//
	// Listens on a specific client for incoming moves
	//
	// Arguments--
	//
	//  clientSocket: the ClientSock object to listen to
	//
	private void listenOnClient(ClientSock clientSocket) {
		while (true) {
			Log.stdout(Log.INFO, "Server", "listenOnClient :: ready to process message from " + clientSocket);
			String recv = this.serverSocket.recv(clientSocket);
			if (recv == null) {
				// For more information about how the client leaving system works, see the comment ~1/2 through the
				// Server.add(Socket) function. In short, when a client leaves this code checks if it was white/black
				// (which are at index 0/1). If so, instead of removing that index, it is set to null and the next
				// client that joins is put in that spot.
				// If the client leaving is not white or black (just a viewer), then the index can be entirely
				// removed to save on memory.
				int leavingClientIndex = this.clientConnections.indexOf(clientSocket);
				if (leavingClientIndex < 2)
					this.clientConnections.set(leavingClientIndex, null);
				else
					this.clientConnections.remove(leavingClientIndex);
				return; // Ignore this client once it has disconnected
			}

			// When something is received from a client, send the response to all clients so the board state is
			// updated for everyone playing or viewing. This can be an unconditional send, as it doesn't matter
			// what order moves get to the server, no valid move will ever be overwritten, ignored, or lost
			String response = this.evaluate(recv);
			this.sendAll(response);
		}
	}
	// end: private void listenOnClient
	

	// ====================================================================================================
	// private void add
	//
	// Adds a new client connection, spawns threads to handle communication with that client, and sends
	// the new client the current state of the board and their assigned color
	//
	// Arguments--
	//
	//  clientConnection: a java Socket object for the client that connected
	//
	private void add(Socket clientConnection) {
		if (clientConnection == null)
			return;

		ClientSock clientSocket = new ClientSock(clientConnection);
		clientSocket.connect(this.ip, this.port);

		// Start each client with an individual thread that calls a localized message parsing method in this Server
		// object. This allows the server to sit each client in a while(true) loop calling recv to get data
		// from the client until something comes back.
		Thread clientThread = new Thread(() -> this.listenOnClient(clientSocket));
		clientThread.start();

		// Find the best place to add the new client, so that existing clients are kept in their place.
		// When clients are removed, the arraylist does not shrink if the client leaving is white or black (which are
		// at indices 0/1). When a new client joins, this for loop checks if either of the first two spots are null
		// and moves the client there to play in the game (instead of at the end to just view).
		int firstNullIndex = this.clientConnections.size();
		for (int i = 0; i < this.clientConnections.size(); i++)
			if (this.clientConnections.get(i) == null) {
				firstNullIndex = i;
				break;
			}

		// Give the new client the current board information and assign them a color based on how many clients
		// are currently connected
		int color = (firstNullIndex < 2) ? (firstNullIndex + 1) : 0;
		Map<String, String> returnData = this.getReturnData();
		returnData.put("color", Integer.toString(color));
		this.serverSocket.send(StringUtility.mapToString(returnData), clientSocket);

		// Add the new client to the list of connected clients using the algorithm mentioned above. In short,
		// either add to the end or insert as WHITE/BLACK
		if (firstNullIndex == this.clientConnections.size())
			this.clientConnections.add(clientSocket);
		else
			this.clientConnections.set(firstNullIndex, clientSocket);
	}
	// end: private void add


	// ====================================================================================================
	// private Map<String, String> getReturnData
	//
	// Formats and returns the basics of a server message
	//
	// Returns--
	//
	//  A map with the keys "fen" and "state"
	//
	private Map<String, String> getReturnData() {
		int boardState = this.board.getInfo().inferState();
		Map<String, String> returnData = new HashMap<>();
		returnData.put("fen", this.board.getInfo().fenString);
		returnData.put("state", Integer.toString(boardState));
		return returnData;
	}
	// end: private Map<String, String> getReturnData
	

	// ====================================================================================================
	// private void sendAll
	//
	// Send a message to all connected clients
	//
	// Arguments--
	//
	//  messasge: the message to send to all clients
	private void sendAll(String message) {
		// Send a message to all clients based on the ClientSock representations. The ClientSock objects here allow
		// use of the IN and OUT buffers to read/write messages. It is up to the actual client on the client-side to
		// call the .recv() method of ClientSock in order to receive the data sent by the server.
		for (ClientSock clientSocket : this.clientConnections) {
			if (clientSocket != null)
				this.serverSocket.send(message, clientSocket);
		}
	}
	// end: private void sendAll

}
// end: public class Server