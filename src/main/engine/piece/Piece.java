// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Piece.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package engine.piece;


import java.io.Serializable;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Piece implements Serializable
//
// A class that represents a piece of a given color and type. Implements the Seriailizable interface
// so it can easily be deep cloned by BoardInfo.clone()
//
public class Piece implements Serializable {

	// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
	// public static final class Type
	//
	// A list of constants for the type a Piece object can have
	//
	public static final class Type {
		public static final int NONE = 0;
		public static final int PAWN = 1;
		public static final int KNIGHT = 2;
		public static final int BISHOP = 3;
		public static final int ROOK = 4;
		public static final int QUEEN = 5;
		public static final int KING = 6;
	}
	// end: pubic static final class Type


	// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
	// public static final class Color
	//
	// A list of constants for the color a Piece object can have
	//
	public static final class Color {
		public static final int NONE = 0;
		public static final int WHITE = 1;
		public static final int BLACK = 2;
	}
	// end: public static final class Color
	

	private int type;
	private int color;


	// ----------------------------------------------------------------------------------------------------
	// public Piece
	//
	// Arguments--
	//
	//  type:  the type of the piece, from Piece.Type
	//
	//  color: the color of the piece, from Piece.Color
	public Piece(int type, int color) {
		this.type = type;
		this.color = color;
	}
	// end: public Piece


	// ====================================================================================================
	// GET methods
	public int getType() {
		return this.type;
	}

	public int getColor() {
		return this.color;
	}

	public boolean friendly(boolean whiteToMove) {
		if ((this.color == Color.WHITE && whiteToMove) ||
			(this.color == Color.BLACK && !whiteToMove))
			return true;
		return false;
	}
	// end: GET methods


	// ====================================================================================================
	// public boolean equals
	//
	// Checks equality between this Piece object and another object. Equality is determined by:
	//  - Equal type
	//  - Equal color
	//
	// Arguments--
	//
	//  obj: another object to compare equality with
	//
	// Returns--
	//
	//  Whether the argument obj is equal to this Piece object
	//
	@Override
	public boolean equals(Object obj) {
		Piece pObj;
		try {
		    pObj = (Piece) obj;
		}
		catch (ClassCastException e) {
			return false;
		}

		if (pObj == null)
			return false;

		if (pObj.getType() == this.type && pObj.getColor() == this.color)
			return true;

		return false;
	}
	// end: public boolean equals


	// ====================================================================================================
	// public String toString
	//
	// Returns this Piece object as a string representation
	//
	// Returns--
	//
	//  The algebraic notation for this Piece object based on type and color. If the type is unknown, then
	//  the string "?" is returned
	//
	@Override
	public String toString() {
		String pieceChar = "?";

		switch (this.type) {
		case Type.PAWN:
			pieceChar = "P";
			break;
		case Type.KNIGHT:
			pieceChar = "N";
			break;
		case Type.BISHOP:
			pieceChar = "B";
			break;
		case Type.ROOK:
			pieceChar = "R";
			break;
		case Type.QUEEN:
			pieceChar = "Q";
			break;
		case Type.KING:
			pieceChar = "K";
			break;
		}

		if (this.color == Color.BLACK)
			pieceChar = pieceChar.toLowerCase();

		return pieceChar;
	}
	// end: public String toString
	
}
// end: public class Piece
