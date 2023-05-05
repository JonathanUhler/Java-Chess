// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Move.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package engine.move;


import util.Log;
import util.Coordinate;
import util.Vector;
import util.StringUtility;
import engine.piece.Piece;
import java.io.Serializable;
import javax.swing.JComboBox;
import java.util.ArrayList;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Move implements Serializable
//
// A class to represent a move from one tile to another, with an option "flag" to represent unusal
// moves or moves of more than 1 piece (i.e. castling). Implements the Serializable interface to
// allow easy byte-by-byte deep-copy in the BoardInfo class
//
public class Move implements Serializable {

	// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
	// public static final class Flag
	//
	// A class to represent the possible flags for a move, used for special cases like castling or promotion
	//
	public static final class Flag {
		// List of possible flags
		public static final int NONE = 0;
		public static final int PAWN_TWO_FORWARD = 1;
		public static final int EN_PASSANT = 2;
		public static final int PROMOTE_KNIGHT = 3;
		public static final int PROMOTE_BISHOP = 4;
		public static final int PROMOTE_ROOK = 5;
		public static final int PROMOTE_QUEEN = 6;
		public static final int CASTLE_KINGSIDE = 7;
		public static final int CASTLE_QUEENSIDE = 8;
		

		// Private variables used to check if a flag is valid, should be updated accordingly if any
		// new flags are added in the future
		private static final int minFlag = 0;
		private static final int maxFlag = 8;

		public static boolean isValid(int flag) {
			return flag >= Flag.minFlag && flag <= Flag.maxFlag;
		}


		// ====================================================================================================
		// public static int inferFlag
		//
		// Infers the flag that should be assigned to a move based on the move locations, piece moved, and
		// some other information.
		//
		// Arguments--
		//
		//  piece:         the piece being moved
		//
		//  startTile:     the tile the piece started on
		//
		//  endTile:       the tile the piece ended on
		//
		//  enPassantTile: the en passant tile if one is available, null otherwise
		//
		// Returns--
		//
		//  One of the integer flags in the Move.Flag class, representing the best-fitting flag for the move
		//
		public static int inferFlag(Piece piece, Coordinate startTile, Coordinate endTile, Coordinate enPassantTile) {
			int color = piece.getColor();

			// Validity check for the data. If a flag cannot be determined because any of the arguments are
			// invalid, then return NONE as the flag
			if (piece == null || startTile == null || endTile == null ||
				!startTile.isValidTile() || !endTile.isValidTile())
				return Flag.NONE;

			// Pawn flags: promotion, two forward, and ep capture
			if (piece.getType() == Piece.Type.PAWN) {
				// As with other places in the codebase, things like the pawn home row/promotion row, and pawn
				// direction change based on who the player is, so that needs to be found, then the list of
				// tiles need to be gatehred
				int promotionRowY = (color == Piece.Color.WHITE) ? 7 : 0;
				ArrayList<Coordinate> promotionRowTiles = new ArrayList<>();
				for (int x = 0; x < 8; x++)
					promotionRowTiles.add(new Coordinate(x, promotionRowY));
				int yOffset = (color == Piece.Color.WHITE) ? 1 : -1; // Which direction pawns move

				// Pawn two forward
				if (startTile.shift(new Vector(0, 2 * yOffset)).equals(endTile))
					return Flag.PAWN_TWO_FORWARD;

				// En passant capture
				else if (enPassantTile != null &&
						 endTile.equals(enPassantTile))
					return Flag.EN_PASSANT;

				// Promotion
				else if (promotionRowTiles.contains(endTile)) {
					String[] promotionOptions = {"Queen", "Rook", "Bishop", "Knight"};
					JComboBox<String> promotionComboBox = new JComboBox<String>(promotionOptions);
					Log.gfxmsg("Promote to...", promotionComboBox);
					String promotionPiece = (String) promotionComboBox.getSelectedItem();
					switch (promotionPiece) {
					case "Queen":
						return Flag.PROMOTE_QUEEN;
					case "Rook":
						return Flag.PROMOTE_ROOK;
					case "Bishop":
						return Flag.PROMOTE_BISHOP;
					case "Knight":
						return Flag.PROMOTE_KNIGHT;
					}
				}
			}
			// King flags: castling king-/queen-side
			else if (piece.getType() == Piece.Type.KING) {
				if (startTile.shift(new Vector(2, 0)).equals(endTile))
					return Flag.CASTLE_KINGSIDE;
				
				else if (startTile.shift(new Vector(-2, 0)).equals(endTile))
					return Flag.CASTLE_QUEENSIDE;
			}

			return Flag.NONE;
		}
		// end: public int inferFlag
	}
	// end: public static final class Flag
	

	private Coordinate startTile;
	private Coordinate endTile;
	private int flag;


	// ----------------------------------------------------------------------------------------------------
	// public Move
	//
	public Move(Coordinate startTile, Coordinate endTile) {
		this(startTile, endTile, Flag.NONE);
	}
	// end: public Move


	// ----------------------------------------------------------------------------------------------------
	// public Move
	//
	// Argments--
	//
	//  startTile: the tile the move starts on
	//
	//  endTile:   the tile the move ends on
	//
	//  flag:      a flag used to define special moves, NONE by default
	//
	public Move(Coordinate startTile, Coordinate endTile, int flag) {
		this.startTile = startTile;
		this.endTile = endTile;

		if (!startTile.isValidTile() ||
			!endTile.isValidTile())
			Log.stdlog(Log.ERROR, "Move", "constructed with invalid start or end tile");

		if (Flag.isValid(flag)) {
			this.flag = flag;
		}
		else {
			Log.stdout(Log.ERROR, "Move", "Move object created with invalid flag, defaulting to NONE");
			this.flag = Flag.NONE;
		}
	}
	// end: public Move


	// ====================================================================================================
	// GET methods
	public Coordinate getStartTile() {
		return this.startTile;
	}

	public Coordinate getEndTile() {
		return this.endTile;
	}

	public int getFlag() {
		return this.flag;
	}

	public boolean isPromotion() {
		if (this.flag == Flag.PROMOTE_KNIGHT ||
			this.flag == Flag.PROMOTE_BISHOP ||
			this.flag == Flag.PROMOTE_ROOK ||
			this.flag == Flag.PROMOTE_QUEEN)
			return true;
		return false;
	}

	public boolean isEnPassant() {
		return this.flag == Flag.EN_PASSANT;
	}

	public boolean isPawnTwoForward() {
		return this.flag == Flag.PAWN_TWO_FORWARD;
	}

	public boolean isCastleKingside() {
		return this.flag == Flag.CASTLE_KINGSIDE;
	}

	public boolean isCastleQueenside() {
		return this.flag == Flag.CASTLE_QUEENSIDE;
	}
	// end: GET methods


	// ====================================================================================================
	// public boolean equals
	//
	// Checks for equality between this Move object and another object. Equality is determined by:
	//  - Equal start tiles
	//  - Equal end tiles
	//  - Equal flags
	//
	// Additionally, the argument obj must be a Move type and cannot be null for it to be equal to this
	//
	// Arguments--
	//
	//  obj: the object to compare equality
	//
	// Returns--
	//
	//  Whether the argument obj is equal to this Move object
	//
	@Override
	public boolean equals(Object obj) {
		Move mObj;
		try {
		    mObj = (Move) obj;
		}
		catch (ClassCastException e) {
			return false;
		}

		if (mObj == null)
			return false;

		if (mObj.getStartTile().equals(this.startTile) &&
			mObj.getEndTile().equals(this.endTile) &&
			mObj.getFlag() == this.flag)
			return true;

		return false;
	}
	// end: public boolean equals


	// ====================================================================================================
	// public String toString
	//
	// Returns a string representation of this Move object
	//
	// Returns--
	//
	//  This move object as a string with the following format:
	//
	//   <start tile> -> <end tile> (flag: <flag>)
	@Override
	public String toString() {
		return StringUtility.coordinateToString(this.startTile) + " -> " +
			StringUtility.coordinateToString(this.endTile) + " (f" + this.flag + ")";
	}
	// end: public String toString

}
// end: public class Move
