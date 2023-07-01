package server;


import jnet.JClientSocket;
import jnet.Log;
import jnet.Bytes;
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
public class ChessServer extends Server {

	private List<JClientSocket> clients;
	

	/**
	 * Constructs a new {@code ChessServer} object. This server implementation supports
	 * a classic game of chess.
	 *
	 * @param ip    the IP address to bind the server to.
	 * @param port  the port to bind the server to.
	 *
	 * @throws IOException  if a network error occurs during server startup.
	 *
	 * @see server.Server
	 */
	public ChessServer(String ip, int port) throws IOException {
		super(ip, port);
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
		case 0 -> color = Piece.Color.WHITE;
		case 1 -> color = Piece.Color.BLACK;
		default -> color = Piece.Color.NONE;
		}

		// Send information
		clientSocket.send(Communication.serialize(Communication.cmdColor(color)));
		clientSocket.send(Communication.serialize(Communication.cmdState(super.getBoardInfo())));
	}


	@Override
	public void clientCommunicated(byte[] recv, JClientSocket clientSocket) {
		String commandStr = Bytes.bytesToString(recv);
		Map<String, String> command = Communication.deserialize(commandStr);
		String opcode = command.get(Communication.KEY_CMD);
		if (opcode == null) {
			Log.stdlog(Log.ERROR, "ChessServer", "null opcode in command: " + command);
			return;
		}

		switch (opcode) {
		case Communication.CMD_MOVE: {
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
						   "ChessServer", "unable to parse command: " + e + ", " + command);
				super.sendBoard(clientSocket);
				return;
			}

			if (!startTile.isValidTile() || !endTile.isValidTile()) {
				Log.stdlog(Log.WARN, "ChessServer", "invalid move tiles: startTile=" +
						   startTile + ", endTile=" + endTile);
				super.sendBoard(clientSocket);
				return;
			}

			// Check the player color
			int position = this.clients.indexOf(clientSocket);
			boolean whiteToMove = super.getBoardInfo().whiteToMove;
			if (!(whiteToMove && position == 0) && !(!whiteToMove && position == 1)) {
				Log.stdlog(Log.WARN, "ChessServer", "invalid color for move: whiteToMove=" +
						   whiteToMove + ", position=" + position);
				super.sendBoard(clientSocket);
				return;
			}

			// Create the move object
			Move move = new Move(startTile, endTile, flag);
			
			// Check if this move is legal
			List<Move> legalMoves = MoveGenerator.generateLegalMoves(super.getBoardInfo());
			if (!legalMoves.contains(move)) {
				Log.stdlog(Log.WARN, "ChessServer", "illegal move attempted: " + move);
				super.sendBoard(clientSocket);
				return;
			}

			// Attempt to make the move
			try {
				super.getBoard().makeMove(move);
			}
			catch (RuntimeException e) {
				Log.stdlog(Log.WARN, "ChessServer", "invalid move attempted: " + e + ", " + move);
				super.sendBoard(clientSocket);
				return;
			}

			// Broadcast new board state
			super.sendBoard();
			break;
		}
		case Communication.CMD_RESTART: {
		    BoardInfo boardInfo = FenUtility.informationFromFen(Board.START_FEN);
			super.setBoardInfo(boardInfo);
		}
		default:
			Log.stdlog(Log.ERROR, "ChessServer", "invalid opcode in command: " + command);
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
