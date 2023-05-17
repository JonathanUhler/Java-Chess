package variants.crazyhouse;


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
import java.util.HashMap;


/**
 * A {@code Server} to manage a classic game of chess.
 *
 * @author Jonathan Uhler
 */
public class CrazyServer extends Server {

	// Special command information
	public static final String CMD_BANK = "bank";
	public static final String CMD_PLACE = "place";
	public static final String KEY_TYPE = "type";
	public static final String KEY_MY_PAWNS = "mypawns";
	public static final String KEY_MY_KNIGHTS = "myknights";
	public static final String KEY_MY_BISHOPS = "mybishops";
	public static final String KEY_MY_ROOKS = "myrooks";
	public static final String KEY_MY_QUEENS = "myqueens";
	public static final String KEY_OP_PAWNS = "oppawns";
	public static final String KEY_OP_KNIGHTS = "opknights";
	public static final String KEY_OP_BISHOPS = "opbishops";
	public static final String KEY_OP_ROOKS = "oprooks";
	public static final String KEY_OP_QUEENS = "opqueens";

	public static Map<String, String> cmdBank(Map<Piece.Type, Integer> myBank,
											  Map<Piece.Type, Integer> opBank)
	{
		if (myBank == null || opBank == null)
			return null;
		
		Map<String, String> map = new HashMap<>();
		map.put(Communication.KEY_CMD, CrazyServer.CMD_BANK);
		map.put(CrazyServer.KEY_MY_PAWNS, Integer.toString(myBank.get(Piece.Type.PAWN)));
		map.put(CrazyServer.KEY_MY_KNIGHTS, Integer.toString(myBank.get(Piece.Type.KNIGHT)));
		map.put(CrazyServer.KEY_MY_BISHOPS, Integer.toString(myBank.get(Piece.Type.BISHOP)));
		map.put(CrazyServer.KEY_MY_ROOKS, Integer.toString(myBank.get(Piece.Type.ROOK)));
		map.put(CrazyServer.KEY_MY_QUEENS, Integer.toString(myBank.get(Piece.Type.QUEEN)));
		map.put(CrazyServer.KEY_OP_PAWNS, Integer.toString(opBank.get(Piece.Type.PAWN)));
		map.put(CrazyServer.KEY_OP_KNIGHTS, Integer.toString(opBank.get(Piece.Type.KNIGHT)));
		map.put(CrazyServer.KEY_OP_BISHOPS, Integer.toString(opBank.get(Piece.Type.BISHOP)));
		map.put(CrazyServer.KEY_OP_ROOKS, Integer.toString(opBank.get(Piece.Type.ROOK)));
		map.put(CrazyServer.KEY_OP_QUEENS, Integer.toString(opBank.get(Piece.Type.QUEEN)));
		return map;
	}


	public static Map<String, String> cmdPlace(Piece.Type pieceType, Coordinate placeTile) {
		if (pieceType == null || placeTile == null)
			return null;

		Map<String, String> map = new HashMap<>();
		map.put(Communication.KEY_CMD, CrazyServer.CMD_PLACE);
		map.put(CrazyServer.KEY_TYPE, pieceType.name());
		map.put(Communication.KEY_END, placeTile.toString());
		return map;
	}


	private Map<Piece.Type, Integer> whiteBank;
	private Map<Piece.Type, Integer> blackBank;
	
	

	// Connected clients
	private List<JClientSocket> clients;
	

	/**
	 * Constructs a new {@code CrazyServer} object.
	 *
	 * @param ip    the IP address to bind the server to.
	 * @param port  the port to bind the server to.
	 *
	 * @throws IOException  if a network error occurs during server startup.
	 *
	 * @see server.Server
	 */
	public CrazyServer(String ip, int port) throws IOException {
		super(ip, port);
	}


	@Override
	public void clientConnected(JClientSocket clientSocket) {
		if (this.clients == null)
			this.clients = new ArrayList<>();
		if (this.whiteBank == null) {
			this.whiteBank = new HashMap<>();
			this.whiteBank.put(Piece.Type.PAWN, 0);
			this.whiteBank.put(Piece.Type.KNIGHT, 0);
			this.whiteBank.put(Piece.Type.BISHOP, 0);
			this.whiteBank.put(Piece.Type.ROOK, 0);
			this.whiteBank.put(Piece.Type.QUEEN, 0);
		}
		if (this.blackBank == null) {
			this.blackBank = new HashMap<>();
			this.blackBank.put(Piece.Type.PAWN, 0);
			this.blackBank.put(Piece.Type.KNIGHT, 0);
			this.blackBank.put(Piece.Type.BISHOP, 0);
			this.blackBank.put(Piece.Type.ROOK, 0);
			this.blackBank.put(Piece.Type.QUEEN, 0);
		}
		
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
			Log.stdlog(Log.ERROR, "CrazyServer", "null opcode in command: " + command);
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
						   "CrazyServer", "unable to parse command: " + e + ", " + command);
				this.sendBankInfo();
				super.sendBoard(clientSocket);
				return;
			}

			if (!startTile.isValidTile() || !endTile.isValidTile()) {
				Log.stdlog(Log.WARN, "CrazyServer", "invalid move tiles: startTile=" +
						   startTile + ", endTile=" + endTile);
				this.sendBankInfo();
				super.sendBoard(clientSocket);
				return;
			}

			// Check the player color
			int position = this.clients.indexOf(clientSocket);
			boolean whiteToMove = super.getBoardInfo().whiteToMove;
			if (!(whiteToMove && position == 0) && !(!whiteToMove && position == 1)) {
				Log.stdlog(Log.WARN, "CrazyServer", "invalid color for move: whiteToMove=" +
						   whiteToMove + ", position=" + position);
				this.sendBankInfo();
				super.sendBoard(clientSocket);
				return;
			}

			// Create the move object
			Move move = new Move(startTile, endTile, flag);
			
			// Check if this move is legal
			List<Move> legalMoves = MoveGenerator.generateLegalMoves(super.getBoardInfo());
			if (!legalMoves.contains(move)) {
				Log.stdlog(Log.WARN, "CrazyServer", "illegal move attempted: " + move);
				this.sendBankInfo();
				super.sendBoard(clientSocket);
				return;
			}

			// Attempt to make the move and handle piece captures
			try {
				Piece capturedPiece = super.getBoardInfo().getPiece(endTile);
				if (flag.equals(Move.Flag.EN_PASSANT)) // Somewhat hacky edge case for ep capture
					capturedPiece = new Piece(Piece.Type.PAWN, Piece.Color.NONE);
				
				super.getBoard().makeMove(move);

				if (capturedPiece != null) {
					Piece.Type type = capturedPiece.getType();
					if (whiteToMove)
						this.whiteBank.put(type, this.whiteBank.get(type) + 1);
					else
						this.blackBank.put(type, this.blackBank.get(type) + 1);

					// Send updated info
				    this.sendBankInfo();
				}
			}
			catch (RuntimeException e) {
				Log.stdlog(Log.WARN, "CrazyServer", "invalid move attempted: " + e + ", " + move);
				super.sendBoard(clientSocket);
				this.sendBankInfo();
				return;
			}

			// Broadcast new board state
			super.sendBoard();
			break;
		}
		case Communication.CMD_RESTART: {
		    BoardInfo boardInfo = FenUtility.informationFromFen(Board.START_FEN);
			this.whiteBank = new HashMap<>();
			this.whiteBank.put(Piece.Type.PAWN, 0);
			this.whiteBank.put(Piece.Type.KNIGHT, 0);
			this.whiteBank.put(Piece.Type.BISHOP, 0);
			this.whiteBank.put(Piece.Type.ROOK, 0);
			this.whiteBank.put(Piece.Type.QUEEN, 0);
			this.blackBank = new HashMap<>();
			this.blackBank.put(Piece.Type.PAWN, 0);
			this.blackBank.put(Piece.Type.KNIGHT, 0);
			this.blackBank.put(Piece.Type.BISHOP, 0);
			this.blackBank.put(Piece.Type.ROOK, 0);
			this.blackBank.put(Piece.Type.QUEEN, 0);
			super.setBoardInfo(boardInfo);
			this.sendBankInfo();
			break;
		}
		case CrazyServer.CMD_PLACE: {
			// Check the player color
			int position = this.clients.indexOf(clientSocket);
			boolean whiteToMove = super.getBoardInfo().whiteToMove;
			if (!(whiteToMove && position == 0) && !(!whiteToMove && position == 1)) {
				Log.stdlog(Log.WARN, "CrazyServer", "invalid color for move: whiteToMove=" +
						   whiteToMove + ", position=" + position);
				super.sendBoard(clientSocket);
				this.sendBankInfo();
				return;
			}
			
			// Get the placed piece type and end tile
			Piece.Type pieceType;
			Coordinate endTile;
			try {
				pieceType = Piece.Type.valueOf(command.get(CrazyServer.KEY_TYPE));
				endTile = Coordinate.fromString(command.get(Communication.KEY_END));
			}
			catch (RuntimeException e) {
				Log.stdlog(Log.ERROR,
						   "CrazyServer", "unable to parse placed type: " + e + ", " + command);
				super.sendBoard(clientSocket);
				this.sendBankInfo();
				return;
			}

			// Attempt to place the piece
			Map<Piece.Type, Integer> bank = whiteToMove ? this.whiteBank : this.blackBank;
			boolean hasPiece = bank.get(pieceType) >= 1;
			if (hasPiece) {
			    List<Move> legalMoves = CrazyMoveGenerator.generateLegalMoves(super.getBoardInfo(),
																			  bank);

				int pieceRow = -1;
				switch (pieceType) {
				case PAWN -> pieceRow = 3;
				case KNIGHT -> pieceRow = 4;
				case BISHOP -> pieceRow = 5;
				case ROOK -> pieceRow = 6;
				case QUEEN -> pieceRow = 7;
				}
				
				Coordinate startTile = new Coordinate(-1, pieceRow);
				Move placement = new Move(startTile, endTile, Move.Flag.NONE);
				Piece.Color pieceColor = whiteToMove ? Piece.Color.WHITE : Piece.Color.BLACK;
				if (legalMoves.contains(placement)) {
					super.getBoardInfoPointer().setPiece(endTile, new Piece(pieceType, pieceColor));
					super.getBoardInfoPointer().updateAfterMove();
					super.sendBoard();
					
					// Remove the piece if placed
					bank.put(pieceType, bank.get(pieceType) - 1);
				}
			}

			// Send updated bank information
		    this.sendBankInfo();
			break;
		}
		default:
			Log.stdlog(Log.ERROR, "CrazyServer", "invalid opcode in command: " + command);
			return;
		}
	}


	private void sendBankInfo() {
		Map<String, String> whiteBankCmd = CrazyServer.cmdBank(this.whiteBank,
															   this.blackBank);
		Map<String, String> blackBankCmd = CrazyServer.cmdBank(this.blackBank,
															   this.whiteBank);
		JClientSocket whiteClientSocket = this.clients.get(0);
		JClientSocket blackClientSocket = this.clients.get(1);
		super.send(Communication.serialize(whiteBankCmd), whiteClientSocket);
		super.send(Communication.serialize(blackBankCmd), blackClientSocket);
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
