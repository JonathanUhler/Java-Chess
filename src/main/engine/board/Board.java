package engine.board;


import engine.util.Coordinate;
import engine.util.Vector;
import engine.move.Move;
import engine.move.MoveGenerator;
import engine.piece.Piece;
import java.util.LinkedList;



/**
 * Provides infrastructure to manipulate a {@code BoardInfo} object. The {@code (make|unmake)Move} 
 * methods of this class perform automatic move validation. The primary representation of a given 
 * chess board is through the {@code BoardInfo} class. This class only allows for higher-level 
 * management of the informational class. This hierarchy exists to allow for the {@code BoardInfo} 
 * object to be easily transported, serialized, and deserialized without having to carry around 
 * the extra methods provided by the {@code Board} class.
 *
 * @author Jonathan Uhler
 */
public class Board {

	/** The starting position of a chess board. */
	public static final String START_FEN =
		"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	

	/** A stack of all previous board states, used to undo moves. */
	private LinkedList<BoardInfo> boardHistory;
	/** The current state of the board. */
	private BoardInfo boardInfo;


	/**
	 * Constructs a new {@code Board} object from a {@code BoardInfo} object.
	 *
	 * @param boardInfo  a {@code BoardInfo} object for this class to manage.
	 */
	public Board(BoardInfo boardInfo) {
		this.boardHistory = new LinkedList<>();
		this.boardInfo = boardInfo;
	}


	/**
	 * Returns this object's {@code BoardInfo} object. The object returned is a deep-copy of the 
	 * actual informational object stored in this class. No reference of the returned object is 
	 * kept by this class, allowing for safe end-user manipulation of the returned object.
	 *
	 * @return this object's {@code BoardInfo} object.
	 */
	public BoardInfo getInfo() {
		return (BoardInfo) this.boardInfo.clone();
	}


	/**
	 * Updates the {@code BoardInfo} object managed by this class with a given {@code Move}. The 
	 * legality of the argument {@code move} is validated before any operation is completed. This 
	 * method will throw exceptions upon failure to perform the move. The {@code Move} class makes 
	 * some attempts to salvage illegal moves upon construction.
	 *
	 * @param move  the move to make.
	 *
	 * @throws NullPointerException      if {@code move == null}.
	 * @throws IllegalArgumentException  if the start tile is not valid, the end tile is not valid, 
	 *                                   or the piece on the start tile is null.
	 *
	 * @see engine.move.Move
	 */
	public void makeMove(Move move) {
		if (move == null)
			throw new NullPointerException("move argument was null");
		
		// Save the current state (before the move) to the history for undoing moves
		this.boardHistory.push(this.getInfo());
		
		// Set up important information
		Coordinate startTile = move.getStartTile();
		Coordinate endTile = move.getEndTile();
		Move.Flag flag = move.getFlag();

		Piece movePiece = boardInfo.getPiece(startTile);
		Piece capturedPiece = boardInfo.getPiece(endTile);

		Coordinate castleKRook = new Coordinate(7, 0);
		Coordinate castleQRook = new Coordinate(0, 0);
		Coordinate castlekRook = new Coordinate(7, 7);
		Coordinate castleqRook = new Coordinate(0, 7);

		// Pawn movement direction changes based on perspective
		int pawnDir = (this.boardInfo.whiteToMove) ? 1 : -1;
		// Need to shift 1 "down" to highlight real pawn for EP
		Vector enPassantVector = new Vector(0, -1 * pawnDir);
		Coordinate enPassantTile = this.boardInfo.enPassantTile;
		Coordinate enPassantPieceTile = null;
		Piece enPassantPiece = null;
		if (enPassantTile != null) {
			enPassantPieceTile = enPassantTile.shift(enPassantVector);
			enPassantPiece = this.boardInfo.getPiece(enPassantPieceTile);
		}

		// Do basic validity checks
		if (!startTile.isValidTile() || !endTile.isValidTile() || movePiece == null)
			throw new IllegalArgumentException("illegal move attempted: " +
											   "startTile is valid: " + startTile.isValidTile() +
											   ", endTile is valid: " + endTile.isValidTile() +
											   ", movePiece: " + movePiece);

		// Update halfmoves
		this.boardInfo.halfmoves++;

		// Caputes (en passant and regular)
		if (capturedPiece != null) {
			this.boardInfo.halfmoves = 0;
			this.boardInfo.setPiece(endTile, null);
		}
		else if (move.isEnPassant()) {
			this.boardInfo.halfmoves = 0;
			this.boardInfo.setPiece(enPassantPieceTile, null);
		}

		// Movement
		this.boardInfo.setPiece(endTile, movePiece);
		this.boardInfo.setPiece(startTile, null);
		if (movePiece.getType().equals(Piece.Type.PAWN))
			this.boardInfo.halfmoves = 0;

		// Castling
		if (move.isCastleKingside()) {
			Coordinate rookStartTile = endTile.shift(new Vector(1, 0));
			Coordinate rookEndTile = endTile.shift(new Vector(-1, 0));
			Piece rook = this.boardInfo.getPiece(rookStartTile);
			this.boardInfo.setPiece(rookEndTile, rook);
			this.boardInfo.setPiece(rookStartTile, null);
		}
		else if (move.isCastleQueenside()) {
			Coordinate rookStartTile = endTile.shift(new Vector(-2, 0));
			Coordinate rookEndTile = endTile.shift(new Vector(1, 0));
			Piece rook = this.boardInfo.getPiece(rookStartTile);
			this.boardInfo.setPiece(rookEndTile, rook);
			this.boardInfo.setPiece(rookStartTile, null);
		}

		// King moved, updating castling rights
		if (movePiece.getType().equals(Piece.Type.KING)) {
			if (this.boardInfo.whiteToMove) {
				this.boardInfo.castleK = false;
				this.boardInfo.castleQ = false;
			}
			else {
				this.boardInfo.castlek = false;
				this.boardInfo.castleq = false;
			}
		}
		// Rook moved, update castling rights
		else if (movePiece.getType().equals(Piece.Type.ROOK)) {
			if (startTile.equals(castleKRook)) this.boardInfo.castleK = false;
			else if (startTile.equals(castleQRook)) this.boardInfo.castleQ = false;
			else if (startTile.equals(castlekRook)) this.boardInfo.castlek = false;
			else if (startTile.equals(castleqRook)) this.boardInfo.castleq = false;
		}
		// Rook captured, update castling rights
		else if (capturedPiece != null && capturedPiece.getType().equals(Piece.Type.ROOK)) {
			if (endTile.equals(castleKRook)) this.boardInfo.castleK = false;
			else if (endTile.equals(castleQRook)) this.boardInfo.castleQ = false;
			else if (endTile.equals(castlekRook)) this.boardInfo.castlek = false;
			else if (endTile.equals(castleqRook)) this.boardInfo.castleq = false;
		}

		// Promotion
		if (move.isPromotion()) {
			Piece.Type type = Piece.Type.NONE;
			Piece.Color color =
				(this.boardInfo.whiteToMove) ?
				Piece.Color.WHITE :
				Piece.Color.BLACK;

			switch (flag) {
			case PROMOTE_KNIGHT:
				type = Piece.Type.KNIGHT;
				break;
			case PROMOTE_BISHOP:
				type = Piece.Type.BISHOP;
				break;
			case PROMOTE_ROOK:
				type = Piece.Type.ROOK;
				break;
			case PROMOTE_QUEEN:
				type = Piece.Type.QUEEN;
				break;
			}

			this.boardInfo.setPiece(endTile, new Piece(type, color));
		}

		// Update after move
		if (move.isPawnTwoForward())
			this.boardInfo.enPassantTile = endTile.shift(enPassantVector);
		else
			this.boardInfo.enPassantTile = null;

		this.boardInfo.updateAfterMove();
	}


	/**
	 * Undoes the next available move in the {@code boardHistory} stack.
	 */
	public void unmakeMove() {
		// If there are moves to be unmade, then get the latest board state saved, remove it from
		// the boardHistory stack to save memory and allow further undos, and then set the current
		// board state to the previous board state
		
		if (this.boardHistory.size() == 0)
			return;
		this.boardInfo = this.boardHistory.pop();
	}

}
