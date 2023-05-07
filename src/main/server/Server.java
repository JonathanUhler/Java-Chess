package server;


import jnet.JServer;
import jnet.JClientSocket;
import jnet.Log;
import engine.board.Board;
import engine.board.BoardInfo;
import engine.fen.FenUtility;
import engine.piece.Piece;
import java.io.IOException;
import javax.swing.JOptionPane;


public class Server extends JServer {

	private Board board;
	private ServerCLI cli;
	

	public Server(String ip, int port) throws IOException {
		super(ip, port);

		BoardInfo boardInfo = FenUtility.informationFromFen(Board.START_FEN);
		this.board = new Board(boardInfo);

		this.cli = new ServerCLI(this);
		this.cli.run();
	}


	protected BoardInfo getBoardInfo() {
		return this.board.getInfo();
	}


	protected void setBoardInfo(BoardInfo boardInfo) {
		this.board = new Board(boardInfo);
		Log.stdout(Log.INFO, "Server", "Board updated");
	}


	@Override
	public void clientConnected(JClientSocket clientSocket) {
		// MARK: test code
		clientSocket.send(Communication.serialize(Communication.cmdColor(Piece.Color.WHITE)));
		clientSocket.send(Communication.serialize(Communication.cmdState(this.board.getInfo())));
	}


	@Override
	public void clientCommunicated(byte[] recv, JClientSocket clientSocket) {
		
	}


	@Override
	public void clientDisconnected(JClientSocket clientSocket) {
		
	}
	

	public static void main(String[] args) {
		JOptionPane.showMessageDialog(null,
									  "The chess server is intended to be hosted\n" +
									  "directly from the client GUI using the \"Host Game\"\n" +
									  "button. Please start the server through that method.\n" +
									  "Join an existing game with the \"Join Game\" button.",
									  "Server Hosting", JOptionPane.PLAIN_MESSAGE);
		System.exit(0);
	}

}


/*
import jnet.JServer;
import jnet.JClientSocket;
import jnet.Log;
import util.StringUtility;
import util.Coordinate;
import tests.PerftTest;
import engine.board.Board;
import engine.board.BoardInfo;
import engine.fen.FenUtility;
import engine.move.Move;
import engine.move.MoveGenerator;
import engine.piece.Piece;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


public class Server extends JServer {
	
	private ServerCLI cli;
	private Board board;


	public Server() throws IOException {
		this(JServer.DEFAULT_IP_ADDR, JServer.DEFAULT_PORT);
	}


	public Server(String ip, int port) throws IOException {
		super(ip, port);

		BoardInfo boardInfo = FenUtility.informationFromFen(Board.START_FEN);
		this.board = new Board(boardInfo);

		this.cli = new ServerCLI(this);
		this.cli.run();
	}


	protected void setBoardInfo(BoardInfo boardInfo, ServerCLI sender) {
		if (!sender.equals(this.cli)) {
			Log.stdlog(Log.WARN, "Server", "setBoardInfo called by illegal sender, ignoring");
			return;
		}
		if (boardInfo == null) {
			Log.stdlog(Log.ERROR, "Server", "CLI: setBoardInfo called with null boardInfo");
			return;
		}
		
		this.board = new Board(boardInfo);
		//this.sendAll(StringUtility.mapToString(this.getBoardState()));
	}
	

	protected BoardInfo getBoardInfo(ServerCLI sender) {
		if (!sender.equals(this.cli)) {
			Log.stdlog(Log.WARN, "Server", "getBoardInfo called by illegal sender, ignoring");
			return null;
		}

		return this.board.getInfo();
	}

	
	public String getIP() {
		return super.getIP();
	}
	

	public int getPort() {
		return super.getPort();
	}


	@Override
	public void clientConnected(JClientSocket clientSocket) {
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


	@Override
	public void clientCommunicated(byte[] recv, JClientSocket clientSocket) {
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


	@Override
	public void clientDisconnected(JClientSocket clientSocket) {
		
	}


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
		Map<String, String> returnData = this.getBoardState();

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

		int color;
		try {
			color = Integer.parseInt(colorString);
		}
		catch (NumberFormatException e) {
			Log.stdlog(Log.ERROR, "Server", "player color received is not a number: " + colorString);
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
		Move.Flag flag = Move.inferFlag(this.board.getInfo().getPiece(startTile),
										startTile, endTile,
										this.board.getInfo().enPassantTile);
		Move move = new Move(startTile, endTile, flag);
		List<Move> legalMoves = MoveGenerator.generateLegalMoves(this.board.getInfo());
		
		if (!legalMoves.contains(move)) {
			Log.stdlog(Log.DEBUG, "Server", "illegal move attempted, ignoring");
			return StringUtility.mapToString(returnData);
		}

		this.board.makeMove(move);

		BoardInfo.State boardState = this.board.getInfo().inferState();
		returnData = this.getBoardState();

		// Reset the game if the previous game finished
		if (boardState != BoardInfo.State.ONGOING) {
			BoardInfo boardInfo = FenUtility.informationFromFen(Board.START_FEN);
			this.board = new Board(boardInfo);
		}
		return StringUtility.mapToString(returnData);
		return null; // MARK: test code
	}


	private Map<String, String> getBoardState() {
		BoardInfo.State boardState = this.board.getInfo().inferState();
		Map<String, String> returnData = new HashMap<>();
		returnData.put("fen", this.board.getInfo().fenString);
		returnData.put("state", Integer.toString(boardState));
		return returnData;
	}

}
*/
