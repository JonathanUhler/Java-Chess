package engine.piece;


import java.io.Serializable;


/**
 * Represents a piece of a given color and type. This class implements the {@code Serializable} 
 * interface to allow for deep-copying by the {@code BoardInfo} class.
 *
 * @author Jonathan Uhler
 */
public class Piece implements Serializable {

	/**
	 * Types that a {@code Piece} object can have.
	 */
	public static enum Type {
		/** No piece. */
		NONE,
		/** Pawn piece. */
		PAWN,
		/** Knight piece. */
		KNIGHT,
		/** Bishop piece. */
		BISHOP,
		/** Rook piece. */
		ROOK,
		/** Queen piece. */
		QUEEN,
		/** King piece. */
		KING
	}


	/**
	 * Colors that a {@code Piece} object can have.
	 */
	public static enum Color {
		/** No color. */
		NONE,
		/** White piece. */
		WHITE,
		/** Black piece. */
		BLACK
	}
	

	/** The piece type. */
	private Piece.Type type;
	/** The piece color. */
	private Piece.Color color;


	/**
	 * Constructs a {@code Piece} object.
	 *
	 * @param type   the type of the piece.
	 * @param color  the color of the piece.
	 */
	public Piece(Piece.Type type, Piece.Color color) {
		this.type = type;
		this.color = color;
	}


	/**
	 * Returns the type of the piece.
	 *
	 * @return the type of the piece.
	 */
	public Piece.Type getType() {
		return this.type;
	}


	/**
	 * Returns the color of the piece.
	 *
	 * @return the color of the piece.
	 */
	public Piece.Color getColor() {
		return this.color;
	}


	/**
	 * Determines whether this piece is white. Identical to 
	 * {@code getColor().equals(Piece.Color.WHITE)}.
	 *
	 * @return true if this piece is white.
	 */
	public boolean isWhite() {
		return this.color.equals(Piece.Color.WHITE);
	}


	/**
	 * Determines whether this piece is black. Identical to 
	 * {@code getColor().equals(Piece.Color.BLACK)}.
	 *
	 * @return true if this piece is black.
	 */
	public boolean isBlack() {
		return this.color.equals(Piece.Color.BLACK);
	}


	/**
	 * Determines whether this piece is of the current player (friendly).
	 *
	 * @param whiteToMove  whether it is the white player's turn to move.
	 *
	 * @return the type of the piece.
	 */
	public boolean friendly(boolean whiteToMove) {
		if ((this.color.equals(Color.WHITE) && whiteToMove) ||
			(this.color.equals(Color.BLACK) && !whiteToMove))
			return true;
		return false;
	}


	/**
	 * Checks for equality between this {@code Piece} object and another object. Equality is
	 * determinted by:
	 * <ul>
	 * <li> Equal type
	 * <li> Equal color
	 * </ul>
	 *
	 * @param obj  another object to compare equality with.
	 *
	 * @return true if the two objects are equal.
	 */
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

		if (pObj.getType().equals(this.type) && pObj.getColor().equals(this.color))
			return true;

		return false;
	}


	/**
	 * Returns a string representation of this {@code Piece} object.
	 * <p>
	 * The string returned follows the chess algebraic notation of the piece type and color. If the
	 * type is NONE, the string "?" is returned. If the color is NONE and the piece is known, the
	 * letter returned is always capital (as if the color was WHITE).
	 *
	 * @return a string representation of this {@code Piece} object
	 */
	@Override
	public String toString() {
		String pieceChar = "?";

		switch (this.type) {
		case PAWN:
			pieceChar = "P";
			break;
		case KNIGHT:
			pieceChar = "N";
			break;
		case BISHOP:
			pieceChar = "B";
			break;
		case ROOK:
			pieceChar = "R";
			break;
		case QUEEN:
			pieceChar = "Q";
			break;
		case KING:
			pieceChar = "K";
			break;
		}

		if (this.color.equals(Color.BLACK))
			pieceChar = pieceChar.toLowerCase();

		return pieceChar;
	}
	
}
