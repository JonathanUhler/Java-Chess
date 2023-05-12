package variants.bughouse;


import jnet.JClientSocket;
import jnet.Log;
import jnet.Bytes;
import server.Server;
import server.Communication;
import engine.board.BoardInfo;
import engine.piece.Piece;
import engine.util.Coordinate;
import engine.move.Move;
import engine.move.MoveGenerator;
import engine.fen.FenUtility;
import engine.board.Board;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;


/**
 * A {@code Server} to manage a classic game of chess.
 *
 * @author Jonathan Uhler
 */
public class BugServer extends Server {

	// 0/1 are white and 2/3 are black. 0/3 and 1/2 are teammates
	private List<JClientSocket> clients;
	private Board board1; // Players 0 and 2
	private Board board2; // Players 1 and 3
	

	/**
	 * Constructs a new {@code BugServer} object.
	 *
	 * @param ip    the IP address to bind the server to.
	 * @param port  the port to bind the server to.
	 *
	 * @throws IOException  if a network error occurs during server startup.
	 *
	 * @see server.Server
	 */
	public BugServer(String ip, int port) throws IOException {
		super(ip, port);
	}


	private Board getBoard(int player) {
		if (this.board1 == null)
			this.board1 = new Board(FenUtility.informationFromFen(Board.START_FEN));
		if (this.board2 == null)
			this.board2 = new Board(FenUtility.informationFromFen(Board.START_FEN));
		
		if (player == 0 || player == 2)
			return this.board1;
		if (player == 1 || player == 3)
			return this.board2;

		return board1; // Default for spectators
	}


	@Override
	public void clientConnected(JClientSocket clientSocket) {
		if (this.clients == null)
			this.clients = new ArrayList<>();
		
		// Add the client to the list of connected clients, prioritizing putting them into
		// a null index in the list (e.g. the white or black player and not a spectator)
		int index = this.clients.indexOf(null);
		if (index == -1)
			this.clients.add(clientSocket);
		else
			this.clients.set(index, clientSocket);

		// Get the color
		int position = this.clients.indexOf(clientSocket);
		Piece.Color color;
		switch (position) {
		case 0, 1 -> color = Piece.Color.WHITE;
		case 2, 3 -> color = Piece.Color.BLACK;
		default -> color = Piece.Color.NONE;
		}

		// Send information
		BoardInfo boardInfo = this.getBoard(position).getInfo();
		clientSocket.send(Communication.serialize(Communication.cmdColor(color)));
		clientSocket.send(Communication.serialize(Communication.cmdState(boardInfo)));
	}


	@Override
	public void clientCommunicated(byte[] recv, JClientSocket clientSocket) {
		String commandStr = Bytes.bytesToString(recv);
		Map<String, String> command = Communication.deserialize(commandStr);
		String opcode = command.get(Communication.KEY_CMD);
		if (opcode == null) {
			Log.stdlog(Log.ERROR, "BugServer", "null opcode in command: " + command);
			return;
		}

		switch (opcode) {
		case Communication.CMD_MOVE: {
			int position = this.clients.indexOf(clientSocket);
			Board board = this.getBoard(position);
			
			Coordinate startTile;
			Coordinate endTile;
			Move.Flag flag;

			// Get and validate the move tiles and flag
			try {
				startTile = Coordinate.fromString(command.get(Communication.KEY_START));
				endTile = Coordinate.fromString(command.get(Communication.KEY_END));
				flag = Move.Flag.valueOf(command.get(Communication.KEY_FLAG));
			}
			catch (RuntimeException e) {
				Log.stdlog(Log.ERROR,
						   "BugServer", "unable to parse command: " + e + ", " + command);
				super.sendBoard(board, clientSocket);
				return;
			}

			if (!startTile.isValidTile() || !endTile.isValidTile()) {
				Log.stdlog(Log.WARN, "BugServer", "invalid move tiles: startTile=" +
						   startTile + ", endTile=" + endTile);
				super.sendBoard(board, clientSocket);
				return;
			}

			// Check the player color
			boolean whiteToMove = board.getInfo().whiteToMove;
			if (!(whiteToMove && (position == 0 || position == 1)) &&
				!(!whiteToMove && (position == 2 || position == 3))) {
				Log.stdlog(Log.WARN, "BugServer", "invalid color for move: whiteToMove=" +
						   whiteToMove + ", position=" + position);
				super.sendBoard(board, clientSocket);
				return;
			}

			// Create the move object
			Move move = new Move(startTile, endTile, flag);
			
			// Check if this move is legal
			List<Move> legalMoves = MoveGenerator.generateLegalMoves(board.getInfo());
			if (!legalMoves.contains(move)) {
				Log.stdlog(Log.WARN, "BugServer", "illegal move attempted: " + move);
				super.sendBoard(board, clientSocket);
				return;
			}

			// Attempt to make the move
			try {
			    board.makeMove(move);
			}
			catch (RuntimeException e) {
				Log.stdlog(Log.WARN, "BugServer", "invalid move attempted: " + e + ", " + move);
				super.sendBoard(board, clientSocket);
				return;
			}

			// Broadcast new board state to the appropriate clients.
			//   0/1 = white, 2/3 = black
			//   0/3 and 1/2 are teammates
			if (position == 0 || position == 2) {
				super.sendBoard(board, this.clients.get(0));
				super.sendBoard(board, this.clients.get(2));
			}
			if (position == 1 || position == 3) {
				super.sendBoard(board, this.clients.get(1));
				super.sendBoard(board, this.clients.get(3));
			}
			break;
		}
		case Communication.CMD_RESTART: {
		    this.board1 = new Board(FenUtility.informationFromFen(Board.START_FEN));
			this.board2 = new Board(FenUtility.informationFromFen(Board.START_FEN));
		}
		default:
			Log.stdlog(Log.ERROR, "BugServer", "invalid opcode in command: " + command);
			return;
		}
	}


	@Override
	public void clientDisconnected(JClientSocket clientSocket) {
		int index = this.clients.indexOf(clientSocket);
		if (index == -1)
			return;

		// If the disconnecting client is one of the players (index 0 or 1) and not a spectator,
		// then set the index to null so the next player that connects can be added there.
		if (index < 2)
			this.clients.set(index, null);
		else
			this.clients.remove(index);
	}

}
