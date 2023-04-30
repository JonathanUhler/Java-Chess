// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Board.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package engine.board;


import util.Log;
import util.Coordinate;
import util.Vector;
import engine.move.Move;
import engine.move.MoveGenerator;
import engine.piece.Piece;
import java.util.ArrayList;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Board
//
// A class to manipulate a BoardInfo object
// 
public class Board {

	public static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	

	// Used by the unmakeMove function. Keeps track of all moves. When moves are unmade, they are popped from the list
	private ArrayList<BoardInfo> boardHistory;
	private BoardInfo boardInfo;

	
	// ----------------------------------------------------------------------------------------------------
	// public Board
	//
	// Arguments--
	//
	//  boardInfo: a BoardInfo object that will be manipulated by the methods in this Board object
	//
	public Board(BoardInfo boardInfo) {
		this.boardHistory = new ArrayList<>();
		this.boardInfo = boardInfo;
	}
	// end: public Board


	// ====================================================================================================
	// GET methods
	public BoardInfo getInfo() {
		// Returns a copy of the interal BoardInfo object
		return (BoardInfo) this.boardInfo.clone();
	}
	// end: GET methods


	// ====================================================================================================
	// public void makeMove
	//
	// Takes a Move object and updates the internal BoardInfo object based on that move
	//
	// Arguments--
	//
	//  move: the move to make, must not be null. The move is assumed to be legal at this point and will
	//        be made (with some exceptions, such as a null piece being moved or a null move)
	//        unconditionally. Move legatily checks should be done by whatever server or local GUI is
	//        hosting the game
	//
	public void makeMove(Move move) {
		if (move == null) {
			Log.stdlog(Log.ERROR, "Board", "makeMove called with null move");
			return;
		}
		
		// Save the current state (before the move) to the history for undoing moves
		this.boardHistory.add(this.getInfo());
		
		// Set up important information
		Coordinate startTile = move.getStartTile();
		Coordinate endTile = move.getEndTile();
		int flag = move.getFlag();

		Piece movePiece = boardInfo.getPiece(startTile);
		Piece capturedPiece = boardInfo.getPiece(endTile);

		Coordinate castleKRook = new Coordinate(7, 0);
		Coordinate castleQRook = new Coordinate(0, 0);
		Coordinate castlekRook = new Coordinate(7, 7);
		Coordinate castleqRook = new Coordinate(0, 7);

		int pawnDir = (this.boardInfo.whiteToMove) ? 1 : -1; // Pawn movement direction changes based on perspective
		Vector enPassantVector = new Vector(0, -1 * pawnDir); // Need to shift 1 "down" to highlight real pawn for EP
		Coordinate enPassantTile = this.boardInfo.enPassantTile;
		Coordinate enPassantPieceTile = null;
		Piece enPassantPiece = null;
		if (enPassantTile != null) {
			enPassantPieceTile = enPassantTile.shift(enPassantVector);
			enPassantPiece = this.boardInfo.getPiece(enPassantPieceTile);
		}

		// Do basic validity checks
		if (!startTile.isValidTile() ||
			!endTile.isValidTile() ||
			movePiece == null) {
			Log.stdlog(Log.ERROR, "Board", "makeMove validity checks failed, ignoring move");
			Log.stdlog(Log.ERROR, "Board", "\tstartTile is valid: " + startTile.isValidTile());
			Log.stdlog(Log.ERROR, "Board", "\tendTile is valid: " + endTile.isValidTile());
			Log.stdlog(Log.ERROR, "Board", "\ttmovePiece is null: " + (movePiece == null));
			return;
		}

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
		if (movePiece.getType() == Piece.Type.PAWN)
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
		if (movePiece.getType() == Piece.Type.KING) {
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
		else if (movePiece.getType() == Piece.Type.ROOK) {
			if (startTile.equals(castleKRook)) this.boardInfo.castleK = false;
			else if (startTile.equals(castleQRook)) this.boardInfo.castleQ = false;
			else if (startTile.equals(castlekRook)) this.boardInfo.castlek = false;
			else if (startTile.equals(castleqRook)) this.boardInfo.castleq = false;
		}
		// Rook captured, update castling rights
		else if (capturedPiece != null && capturedPiece.getType() == Piece.Type.ROOK) {
			if (endTile.equals(castleKRook)) this.boardInfo.castleK = false;
			else if (endTile.equals(castleQRook)) this.boardInfo.castleQ = false;
			else if (endTile.equals(castlekRook)) this.boardInfo.castlek = false;
			else if (endTile.equals(castleqRook)) this.boardInfo.castleq = false;
		}

		// Promotion
		if (move.isPromotion()) {
			int type = Piece.Type.NONE;
			int color = (this.boardInfo.whiteToMove) ? Piece.Color.WHITE : Piece.Color.BLACK;

			switch (flag) {
			case Move.Flag.PROMOTE_KNIGHT:
				type = Piece.Type.KNIGHT;
				break;
			case Move.Flag.PROMOTE_BISHOP:
				type = Piece.Type.BISHOP;
				break;
			case Move.Flag.PROMOTE_ROOK:
				type = Piece.Type.ROOK;
				break;
			case Move.Flag.PROMOTE_QUEEN:
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
	// end: public void makeMove


	// ====================================================================================================
	// public void unmakeMove
	//
	// Unmakes the next available move in the boardHistory stack
	//
	public void unmakeMove() {
		// If there are moves to be unmade, then get the latest board state saved, remove it from the
		// boardHistory stack to save memory and allow further undos, and then set the current
		// board state to the previous board state
		
		if (this.boardHistory.size() == 0)
			return;

		int previousStateIndex = this.boardHistory.size() - 1;
		BoardInfo previousState = this.boardHistory.get(previousStateIndex);

		this.boardHistory.remove(previousState);
		this.boardInfo = previousState;
	}
	// end: public void unmakeMove

}
// end: public class Board
