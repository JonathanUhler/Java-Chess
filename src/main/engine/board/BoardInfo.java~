// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// BoardInfo.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package engine.board;


import util.Log;
import util.Coordinate;
import engine.fen.FenUtility;
import engine.piece.Piece;
import engine.move.Move;
import engine.move.MoveGenerator;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class BoardInfo implements Serializable
//
// Tracks all of the information that defines a game of chess, and makes data communication between
// a Board object and other classes easier
//
// NOTE: while the fields in this class are public, the object of this class contained within Board.java
//       should NEVER be public, thus the fields in this class are still safe
//
public class BoardInfo implements Serializable {

	// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
	// public static final class State
	//
	// A list of constants for all the possible end states of a chess game
	//
	public static final class State {
		public static final int ONGOING = 0;
		public static final int WIN_WHITE = 1;
		public static final int WIN_BLACK = 2;
		public static final int DRAW_STALEMATE = 3;
		public static final int DRAW_FIFTY_MOVE = 4;
		public static final int DRAW_REPETITION = 5;
		public static final int DRAW_MATERIAL = 6; // Unused as this engine cannot calculate insufficient material
	}
	// end: public static final class State
	

	public String fenString;
	private Piece[][] tiles; // Only private field, explained more in getPiece(Coordinate)

	public boolean whiteToMove;

	public boolean castleK;
	public boolean castleQ;
	public boolean castlek;
	public boolean castleq;

	public Coordinate enPassantTile;
	public int halfmoves;
	public int fullmoves;

	public HashMap<String, Integer> threefoldRepetitionTracker;
	public ArrayList<Coordinate> tilesOpponentControls;


	// ----------------------------------------------------------------------------------------------------
	// public BoardInfo
	//
	// A constuctor that should only be used by the FenUtility.informationFromFen method because of its
	// complexity. When generating a BoardInfo object, that method (along with a fen string passed in)
	// should be used over the raw access of this constructor
	//
	// Arguments--
	//
	//  tiles:         a 2d array of pieces, representing the pieces on each tile of the board. If there
	//                 is not a piece on a given tile, that index is null
	//
	//  whiteToMove:   whether is it the white player's turn to move
	//
	//  castleK:       whether white castle kingside is allowed
	//
	//  castleQ:       whether white castle queenside is allowed
	//
	//  castlek:       whether black castle kingside is allowed
	//
	//  castleq:       whether black castle queenside is allowed
	//
	//  enPassantTile: the tile available for en passant (the tile a pawn would move to for an en passant
	//                 move, NOT the tile of the pawn to be captured). If no en passant tile exists, pass
	//                 in null
	//
	//  halfmoves:     the number of half moves (moves by either side), used for the 50-move rule
	// 
	//  fullmoves:     the number of full moves
	//
	public BoardInfo(Piece[][] tiles, boolean whiteToMove,
					 boolean castleK, boolean castleQ, boolean castlek, boolean castleq,
					 Coordinate enPassantTile, int halfmoves, int fullmoves) {
		this.tiles = tiles;
		if (tiles == null || tiles.length != 8 || tiles[0].length != 8) {
			Log.stdlog(Log.ERROR, "BoardInfo", "constructed with invalid tiles (null or illegal size)");
			Log.stdlog(Log.ERROR, "BoardInfo", "\ttiles=" + tiles);
			this.tiles = new Piece[8][8];
		}
		
		this.whiteToMove = whiteToMove;

		this.castleK = castleK;
		this.castleQ = castleQ;
		this.castlek = castlek;
		this.castleq = castleq;
		
		this.enPassantTile = enPassantTile;
		this.halfmoves = halfmoves;
		this.fullmoves = fullmoves;
		
		this.threefoldRepetitionTracker = new HashMap<>();
		this.tilesOpponentControls = new ArrayList<>();

		this.fenString = FenUtility.fenFromInformation(this);

		this.update();
	}
	// end: public BoardInfo


	// ====================================================================================================
	// public Piece getPiece
	//
	// Gets a piece from the list of pieces representing the board given a valid coordinate on the board.
	// The "tiles" field of the BoardInfo class, which is accessed by this method, is the only private field
	// in order to avoid access confusion with row-/col-major order.
	//
	//  The X/Y coordinate system (used by the Coordinate class, where the chess board represents quadrant I
	//  on the 2d plane [with the origin at a1]) is in column-major order.
	//
	//  The access into the tiles array is in row-major order. This could lead to some confusion with
	//  programmers accessing "tiles" like: "tiles[c.getX()][c.getY()]" which would be incorrect.
	//
	// Arguments--
	//
	//  c: the coordinate to get the piece of
	//
	// Returns--
	//
	//  The Piece object if one exists at coordinate c, or null if no piece exists or the coordinate is
	//  out of range for the board
	//
	public Piece getPiece(Coordinate c) {
		if (!c.isValidTile()) {
			Log.stdlog(Log.ERROR, "BoardInfo", "getPiece called with invalid coordinate: " + c);
			return null;
		}
		return this.tiles[c.getY()][c.getX()];
	}
	// end: public Piece getPiece


	// ====================================================================================================
	// public void setPiece
	//
	// Sets a given piece on a given tile. This method exists instead of allowing direct access for the
	// reason described in the getPiece documentation. Also note, the piece will only be placed if
	// the coordinate c is in range of the board.
	//
	// Arguments--
	//
	//  c: the coordinate to place the piece on
	//
	//  p: the piece to place on coordinate c
	//
	public void setPiece(Coordinate c, Piece p) {
		if (!c.isValidTile()) {
			Log.stdlog(Log.ERROR, "BoardInfo", "setPiece called with invalid coordinate: " + c);
			return;
		}
		this.tiles[c.getY()][c.getX()] = p;
	}
	// end: public void setPiece


	// ====================================================================================================
	// public ArrayList<Coordinate> getTilesWithPiece
	//
	// Returns a list of Coordinate objects, where each of the Coordinates points to a tile containing
	// a piece of the same type/color as the argument
	//
	// Arguments--
	//
	//  piece: a piece object with the type/color of pieces to search for
	//
	// Returns--
	//
	//  A list of tiles with pieces equal to the argument piece
	//
	public ArrayList<Coordinate> getTilesWithPiece(Piece piece) {
		ArrayList<Coordinate> tilesWithPiece = new ArrayList<>();
		for (Coordinate c : Coordinate.getAllValidCoordinates()) {
			Piece pieceOnTile = this.getPiece(c);
			// Search through every valid coordinate and use the equals() method of the piece object to
			// check equality quickly
			if (pieceOnTile != null && pieceOnTile.equals(piece))
				tilesWithPiece.add(c);
		}
		return tilesWithPiece;
	}
	// end: public ArrayList<Coordinate> getTilesWithPiece


	// ====================================================================================================
	// public int inferState
	//
	// Given all the information present in this object, infers the state of the game (based on whose turn
	// it is, who is in check, number of legal moves to be made, etc.) and returns the assumed state
	//
	// Returns--
	//
	//  An integer representing one of the states in the BoardInfo.State class
	//
	public int inferState() {
		// Get information about the tile the king is on. If there is not exactly 1 king piece for the
		// current player then no state can be inferred, so assume the game is ongoing
		int kingColor = (this.whiteToMove) ? Piece.Color.WHITE : Piece.Color.BLACK;
		Piece kingPiece = new Piece(Piece.Type.KING, kingColor);
		ArrayList<Coordinate> kingTiles = this.getTilesWithPiece(kingPiece);
		if (kingTiles.size() != 1)
			return BoardInfo.State.ONGOING;

		// Get the information needed to determine the state of the game:
		//  - The number of legal moves for the current player, to determine check vs checkmate, and for
		//    stalemate
		//  - The tile the king is on, to determine if the king is under attack
		//  - Whether the king is in check
		int numLegalMoves = MoveGenerator.generateLegalMoves(this).size();
		Coordinate kingTile = kingTiles.get(0);
		boolean inCheck = this.tilesOpponentControls.contains(kingTile);

		// Combinations of states:
		//  - Win for black: the white player has no legal moves and is in check
		//  - Win for white: the black player has no legal moves and is in check
		//  - Stalemate: the current player has no legal moves but is NOT in check (doesn't matter which
		//               player's turn it is, as the final state of the game is a draw)
		//  - 50 move rule: the number of halfmoves since the last pawn move or piece capture is >= 50
		//  - Repetition: the same board layout has occured >= 3 times throughout the game
		//  - Ongoing: none of the above sets of conditions are true
		if (numLegalMoves == 0 && inCheck && this.whiteToMove)
			return BoardInfo.State.WIN_BLACK;
		else if (numLegalMoves == 0 && inCheck && !this.whiteToMove)
			return BoardInfo.State.WIN_WHITE;
		else if (numLegalMoves == 0 && !inCheck)
			return BoardInfo.State.DRAW_STALEMATE;
		else if (this.halfmoves >= 50)
			return BoardInfo.State.DRAW_FIFTY_MOVE;
		else {
			for (int numOccurences : this.threefoldRepetitionTracker.values()) {
				if (numOccurences >= 3)
					return BoardInfo.State.DRAW_REPETITION;
			}
		}

		return BoardInfo.State.ONGOING;
	}
	// end: public int inferState


	// ====================================================================================================
	// public void updateAfterMove
	//
	// Updates this object after a move has been made. This "update" is defined through some common
	// changes after each move:
	//
	// Game/chess related:
	//  - Update the number of fullmoves when appropriate
	//  - Change the player
	//
	// Internal/programming related:
	//  - Update the list of the tiles controlled by the opponent player
	//  - Update the fen string
	//  - Add the new fen string to the threefold tracker
	//
	public void updateAfterMove() {
		// Update fullmoves
		if (!this.whiteToMove)
			this.fullmoves++;
		
		// Update turn
		this.whiteToMove = !this.whiteToMove;

		// Do the rest of the updates, which are also done in the constructor
		this.update();
	}
	// end: public void updateAfterMove


	// ====================================================================================================
	// private void update
	//
	// Updates internal data structures. To updates game related things (like the current player), call
	// updateAfterMove() which also calls this update routine
	//
	// Internal/programming related:
	//  - Update the list of the tiles controlled by the opponent player
	//  - Update the fen string
	//  - Add the new fen string to the threefold tracker
	//
	private void update() {
		// Update tilesOpponentControls
		BoardInfo opponentInfo = (BoardInfo) this.clone();
		opponentInfo.whiteToMove = !this.whiteToMove;
	    this.tilesOpponentControls = MoveGenerator.generateTilesControlled(opponentInfo);

		// Update fen string
		this.fenString = FenUtility.fenFromInformation(this);

		// Update threefold repetition
		String posString = this.fenString.split(" ")[0]; // Position part (first element) of fen string
		if (this.threefoldRepetitionTracker.containsKey(posString)) {
			int numOccurences = this.threefoldRepetitionTracker.get(posString);
			this.threefoldRepetitionTracker.put(posString, numOccurences + 1);
		}
		else
			this.threefoldRepetitionTracker.put(posString, 1);
	}
	// end: private void update


	// ====================================================================================================
	// public Object clone
	//
	// Returns a deep-copy of this BoardInfo object
	// Source: https://stackoverflow.com/questions/64036/how-do-you-make-a-deep-copy-of-an-object
	//
	// Returns--
	//
	//  An Object-type object that can be cast to a BoardInfo object if the clone was successful,
	//  otherwise null
	//
	@Override
	public Object clone() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this);
			oos.flush();
			oos.close();
			bos.close();

			byte[] byteData = bos.toByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(byteData);
			Object obj = new ObjectInputStream(bis).readObject();

			return obj;
		}
		catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	// end: public Object clone


	// ====================================================================================================
	// public String toString
	//
	// Returns a string representation of this BoardInfo object
	//
	// Returns--
	//
	//  A string composed of:
	//   - The fen string of this BoardInfo object
	//   - An 8*8 grid of "." for a blank tile or a letter (following the notation of FEN strings) for
	//     a piece
	//
	@Override
	public String toString() {
		String toString = FenUtility.fenFromInformation(this) + "\n";

		for (int y = 7; y >= 0; y--) {
			for (int x = 0; x < 8; x++) {
				Piece piece = this.getPiece(new Coordinate(x, y));
				if (piece == null)
					toString += ". ";
				else
					toString += piece + " ";
			}
			toString += "\n";
		}

		return toString;
	}
	// end: public String toString

}
// end: public class BoardInfo
