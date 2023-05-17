package variants.bughouse;


import jnet.JClientSocket;
import jnet.Log;
import jnet.Bytes;
import server.Server;
import server.Communication;
import engine.board.Board;
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


public class BugServer extends Server {

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
		map.put(Communication.KEY_CMD, BugServer.CMD_BANK);
		map.put(BugServer.KEY_MY_PAWNS, Integer.toString(myBank.get(Piece.Type.PAWN)));
		map.put(BugServer.KEY_MY_KNIGHTS, Integer.toString(myBank.get(Piece.Type.KNIGHT)));
		map.put(BugServer.KEY_MY_BISHOPS, Integer.toString(myBank.get(Piece.Type.BISHOP)));
		map.put(BugServer.KEY_MY_ROOKS, Integer.toString(myBank.get(Piece.Type.ROOK)));
		map.put(BugServer.KEY_MY_QUEENS, Integer.toString(myBank.get(Piece.Type.QUEEN)));
		map.put(BugServer.KEY_OP_PAWNS, Integer.toString(opBank.get(Piece.Type.PAWN)));
		map.put(BugServer.KEY_OP_KNIGHTS, Integer.toString(opBank.get(Piece.Type.KNIGHT)));
		map.put(BugServer.KEY_OP_BISHOPS, Integer.toString(opBank.get(Piece.Type.BISHOP)));
		map.put(BugServer.KEY_OP_ROOKS, Integer.toString(opBank.get(Piece.Type.ROOK)));
		map.put(BugServer.KEY_OP_QUEENS, Integer.toString(opBank.get(Piece.Type.QUEEN)));
		return map;
	}


	public static Map<String, String> cmdPlace(Piece.Type pieceType, Coordinate placeTile) {
		if (pieceType == null || placeTile == null)
			return null;

		Map<String, String> map = new HashMap<>();
		map.put(Communication.KEY_CMD, BugServer.CMD_PLACE);
		map.put(BugServer.KEY_TYPE, pieceType.name());
		map.put(Communication.KEY_END, placeTile.toString());
		return map;
	}


	private Map<Piece.Type, Integer> white1Bank; // 0
	private Map<Piece.Type, Integer> black1Bank; // 1
	private Map<Piece.Type, Integer> white2Bank; // 2
	private Map<Piece.Type, Integer> black2Bank; // 3
	
	

	// Connected clients
	// Player composition:
	/*
	  W B
      0 1
      2 3
	  
	  Teammate = horizontal
	  Opponent = diagonal
	 */
	// 0/2 = white, 1/3 = black. 0/1, 2/3 = teammates. 0/3, 1/2 = opponents.
	private List<JClientSocket> clients;
	private Board board1; // Players 0 and 3
	private Board board2; // Players 1 and 2
	

	public BugServer(String ip, int port) throws IOException {
		super(ip, port);
	}


	private Board getBoard(int player) {
		if (this.board1 == null)
			this.board1 = new Board(FenUtility.informationFromFen(Board.START_FEN));
		if (this.board2 == null)
			this.board2 = new Board(FenUtility.informationFromFen(Board.START_FEN));

		if (player == 0 || player == 3)
			return this.board1;
		if (player == 1 || player == 2)
			return this.board2;
		return null;
	}


	private Map<Piece.Type, Integer> getBank(int position) {
		switch (position) {
		case 0:
			return this.white1Bank;
		case 1:
			return this.black1Bank;
		case 2:
			return this.white2Bank;
		case 3:
			return this.black2Bank;
		}
		return null;
	}


	@Override
	public void clientConnected(JClientSocket clientSocket) {
		if (this.clients == null)
			this.clients = new ArrayList<>();
		if (this.white1Bank == null) {
			this.white1Bank = new HashMap<>();
			this.white1Bank.put(Piece.Type.PAWN, 0);
			this.white1Bank.put(Piece.Type.KNIGHT, 0);
			this.white1Bank.put(Piece.Type.BISHOP, 0);
			this.white1Bank.put(Piece.Type.ROOK, 0);
			this.white1Bank.put(Piece.Type.QUEEN, 0);
		}
		if (this.black1Bank == null) {
			this.black1Bank = new HashMap<>();
			this.black1Bank.put(Piece.Type.PAWN, 0);
			this.black1Bank.put(Piece.Type.KNIGHT, 0);
			this.black1Bank.put(Piece.Type.BISHOP, 0);
			this.black1Bank.put(Piece.Type.ROOK, 0);
			this.black1Bank.put(Piece.Type.QUEEN, 0);
		}
		if (this.white2Bank == null) {
			this.white2Bank = new HashMap<>();
			this.white2Bank.put(Piece.Type.PAWN, 0);
			this.white2Bank.put(Piece.Type.KNIGHT, 0);
			this.white2Bank.put(Piece.Type.BISHOP, 0);
			this.white2Bank.put(Piece.Type.ROOK, 0);
			this.white2Bank.put(Piece.Type.QUEEN, 0);
		}
		if (this.black2Bank == null) {
			this.black2Bank = new HashMap<>();
			this.black2Bank.put(Piece.Type.PAWN, 0);
			this.black2Bank.put(Piece.Type.KNIGHT, 0);
			this.black2Bank.put(Piece.Type.BISHOP, 0);
			this.black2Bank.put(Piece.Type.ROOK, 0);
			this.black2Bank.put(Piece.Type.QUEEN, 0);
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
		case 0, 2 -> color = Piece.Color.WHITE;
		case 1, 3 -> color = Piece.Color.BLACK;
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
			BoardInfo boardInfo = board.getInfo();
			
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
			boolean whiteToMove = boardInfo.whiteToMove;
			if (!(whiteToMove && (position == 0 || position == 2)) &&
				!(!whiteToMove && (position == 1 || position == 3)))
			{
				Log.stdlog(Log.WARN, "BugServer", "invalid color for move: whiteToMove=" +
						   whiteToMove + ", position=" + position);
				super.sendBoard(board, clientSocket);
				return;
			}

			// Create the move object
			Move move = new Move(startTile, endTile, flag);
			
			// Check if this move is legal
			List<Move> legalMoves = MoveGenerator.generateLegalMoves(boardInfo);
			if (!legalMoves.contains(move)) {
				Log.stdlog(Log.WARN, "BugServer", "illegal move attempted: " + move);
				super.sendBoard(board, clientSocket);
				return;
			}

			// Attempt to make the move and handle piece captures
			try {
				Piece capturedPiece = boardInfo.getPiece(endTile);
				if (flag.equals(Move.Flag.EN_PASSANT)) // Somewhat hacky edge case for ep capture
					capturedPiece = new Piece(Piece.Type.PAWN, Piece.Color.NONE);
				
			    board.makeMove(move);

				if (capturedPiece != null) {
					Piece.Type type = capturedPiece.getType();
					switch (position) {
					case 0 -> this.black1Bank.put(type, this.black1Bank.get(type) + 1);
					case 1 -> this.white1Bank.put(type, this.white1Bank.get(type) + 1);
					case 2 -> this.black2Bank.put(type, this.black2Bank.get(type) + 1);
					case 3 -> this.white2Bank.put(type, this.white2Bank.get(type) + 1);
					}

					// Send updated info
				    this.sendBankInfo(position, true);
				}
			}
			catch (RuntimeException e) {
				Log.stdlog(Log.WARN, "BugServer", "invalid move attempted: " + e + ", " + move);
				super.sendBoard(board, clientSocket);
				return;
			}

			// Broadcast new board state
		    if (position == 0 || position == 3) {
				super.sendBoard(board, this.clients.get(0));
				super.sendBoard(board, this.clients.get(3));
			}
			if (position == 1 || position == 2) {
				super.sendBoard(board, this.clients.get(1));
				super.sendBoard(board, this.clients.get(2));
			}
			break;
		}
		case Communication.CMD_RESTART: {
		    BoardInfo boardInfo = FenUtility.informationFromFen(Board.START_FEN);
			this.white1Bank = new HashMap<>();
			this.white1Bank.put(Piece.Type.PAWN, 0);
			this.white1Bank.put(Piece.Type.KNIGHT, 0);
			this.white1Bank.put(Piece.Type.BISHOP, 0);
			this.white1Bank.put(Piece.Type.ROOK, 0);
			this.white1Bank.put(Piece.Type.QUEEN, 0);
			this.black1Bank = new HashMap<>();
			this.black1Bank.put(Piece.Type.PAWN, 0);
			this.black1Bank.put(Piece.Type.KNIGHT, 0);
			this.black1Bank.put(Piece.Type.BISHOP, 0);
			this.black1Bank.put(Piece.Type.ROOK, 0);
			this.black1Bank.put(Piece.Type.QUEEN, 0);
			this.white2Bank = new HashMap<>();
			this.white2Bank.put(Piece.Type.PAWN, 0);
			this.white2Bank.put(Piece.Type.KNIGHT, 0);
			this.white2Bank.put(Piece.Type.BISHOP, 0);
			this.white2Bank.put(Piece.Type.ROOK, 0);
			this.white2Bank.put(Piece.Type.QUEEN, 0);
			this.black2Bank = new HashMap<>();
			this.black2Bank.put(Piece.Type.PAWN, 0);
			this.black2Bank.put(Piece.Type.KNIGHT, 0);
			this.black2Bank.put(Piece.Type.BISHOP, 0);
			this.black2Bank.put(Piece.Type.ROOK, 0);
			this.black2Bank.put(Piece.Type.QUEEN, 0);
		    this.board1 = new Board(FenUtility.informationFromFen(Board.START_FEN));
			this.board2 = new Board(FenUtility.informationFromFen(Board.START_FEN));
			this.sendBoard(this.board1, this.clients.get(0));
			this.sendBoard(this.board1, this.clients.get(3));
			this.sendBoard(this.board2, this.clients.get(1));
			this.sendBoard(this.board2, this.clients.get(2));
			this.sendBankInfo(0, true);
			this.sendBankInfo(1, true);
			break;
		}
		case BugServer.CMD_PLACE: {
			int position = this.clients.indexOf(clientSocket);
			Board board = this.getBoard(position);
			BoardInfo boardInfo = board.getInfo();
			BoardInfo boardInfoPointer = board.getInfoPointer();
			
		    // Check the player color
			boolean whiteToMove = boardInfo.whiteToMove;
			if (!(whiteToMove && (position == 0 || position == 2)) &&
				!(!whiteToMove && (position == 1 || position == 3)))
			{
				Log.stdlog(Log.WARN, "BugServer", "invalid color for move: whiteToMove=" +
						   whiteToMove + ", position=" + position);
				super.sendBoard(board, clientSocket);
				this.sendBankInfo(position, false);
				return;
			}
			
			// Get the placed piece type and end tile
			Piece.Type pieceType;
			Coordinate endTile;
			try {
				pieceType = Piece.Type.valueOf(command.get(BugServer.KEY_TYPE));
				endTile = Coordinate.fromString(command.get(Communication.KEY_END));
			}
			catch (RuntimeException e) {
				Log.stdlog(Log.ERROR,
						   "BugServer", "unable to parse placed type: " + e + ", " + command);
				super.sendBoard(clientSocket);
				this.sendBankInfo(position, false);
				return;
			}

			// Attempt to place the piece
			Map<Piece.Type, Integer> bank = this.getBank(position);
			boolean hasPiece = bank.get(pieceType) >= 1;
			if (hasPiece) {
			    List<Move> legalMoves = BugMoveGenerator.generateLegalMoves(boardInfo, bank);

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
				    boardInfoPointer.setPiece(endTile, new Piece(pieceType, pieceColor));
				    boardInfoPointer.updateAfterMove();
					
					// Remove the piece if placed
					bank.put(pieceType, bank.get(pieceType) - 1);

					// Broadcast new board state
					if (position == 0 || position == 3) {
						super.sendBoard(board, this.clients.get(0));
						super.sendBoard(board, this.clients.get(3));
					}
					if (position == 1 || position == 2) {
						super.sendBoard(board, this.clients.get(1));
						super.sendBoard(board, this.clients.get(2));
					}
				}
			}

			// Send updated bank information
		    this.sendBankInfo(position, false);
			break;
		}
		default:
			Log.stdlog(Log.ERROR, "BugServer", "invalid opcode in command: " + command);
			return;
		}
	}


	private void sendBankInfo(int position, boolean wasCapture) {
		Map<Piece.Type, Integer> whiteBank = null;
		Map<Piece.Type, Integer> blackBank = null;
		int whitePlayer = 0;
		int blackPlayer = 0;
		switch (position) {
		case 0, 3:
			whiteBank = wasCapture ? this.white2Bank : this.white1Bank;
			blackBank = wasCapture ? this.black1Bank : this.black2Bank;
			whitePlayer = wasCapture ? 2 : 0;
			blackPlayer = wasCapture ? 1 : 3;
			break;
		case 1, 2:
			whiteBank = wasCapture ? this.white1Bank : this.white2Bank;
			blackBank = wasCapture ? this.black2Bank : this.black1Bank;
			whitePlayer = wasCapture ? 0 : 2;
			blackPlayer = wasCapture ? 3 : 1;
			break;
		}
		
		Map<String, String> whiteBankCmd = BugServer.cmdBank(whiteBank, blackBank);
		Map<String, String> blackBankCmd = BugServer.cmdBank(blackBank, whiteBank);
		JClientSocket whiteClientSocket = this.clients.get(whitePlayer);
		JClientSocket blackClientSocket = this.clients.get(blackPlayer);
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
