package engine.move;


import engine.util.Coordinate;
import engine.util.Vector;
import engine.piece.Piece;
import java.io.Serializable;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.ArrayList;


/**
 * Represents a move from one tile to another with an optional flat to represent special moves. 
 * This class implements the {@code Serializable} interface to allow deep-copying by the 
 * {@code BoardInfo} class.
 *
 * @author Jonathan Uhler
 */
public class Move implements Serializable {

    /**
	 * Represents a special or conditional move. These include: moving pawns two forward, 
	 * en passant capture, promotion, and castling.
	 */
	public static enum Flag {
		/** No special flag; this is an ordinary move. */
	    NONE,
		/** A pawn has been moved two tiles forward for its first move. */
		PAWN_TWO_FORWARD,
		/** A pawn has captured through en passant. */
		EN_PASSANT,
		/** A pawn has been promoted to a knight. */
		PROMOTE_KNIGHT,
		/** A pawn has been promoted to a bishop. */
		PROMOTE_BISHOP,
		/** A pawn has been promoted to a rook. */
		PROMOTE_ROOK,
		/** A pawn has been promoted to a queen. */
		PROMOTE_QUEEN,
		/** A kingside castle was performed. */
		CASTLE_KINGSIDE,
		/** A queenside castle was performed. */
		CASTLE_QUEENSIDE
	}
	

	/** The origin of the piece. */
	private Coordinate startTile;
	/** The destination of the piece. */
	private Coordinate endTile;
	/** The move flag, for special move types. */
	private Move.Flag flag;


	/**
	 * Constructs a {@code Move} object between two tiles.
	 *
	 * @param startTile  the origin of the piece being moved.
	 * @param endTile    the destination of the piece being moved.
	 *
	 * @see Move(Coordinate, Coordinate, Move.Flag)
	 */
	public Move(Coordinate startTile, Coordinate endTile) {
		this(startTile, endTile, Flag.NONE);
	}


	/**
	 * Constructs a {@code Move} object between two tiles.
	 *
	 * @param startTile  the origin of the piece being moved.
	 * @param endTile    the destination of the piece being moved.
	 * @param flag the   special flag for this move.
	 *
	 * @throws NullPointerException      if {@code startTile} or {@code endTile} is null.
	 * @throws NullPointerException      if {@code flag} is null.
	 * @throws IllegalArgumentException  if {@code startTile} or {@code endTile} is invalid.
	 */
	public Move(Coordinate startTile, Coordinate endTile, Move.Flag flag) {
		if (startTile == null || endTile == null)
			throw new NullPointerException("tiles were null: found startTile=" +
										   startTile + ", endTile=" + endTile);
		if (!startTile.isValidTile() || !endTile.isValidTile())
			throw new IllegalArgumentException("tiles were invalid: startTile valid: " +
											   startTile.isValidTile() + ", endTile valid: " +
											   endTile.isValidTile());
		if (flag == null)
			throw new NullPointerException("flag was null");
		
		this.startTile = startTile;
		this.endTile = endTile;
	    this.flag = flag;
	}


    /**
	 * Gets the start tile of this move.
	 *
	 * @return the start tile of this move.
	 */
	public Coordinate getStartTile() {
		return this.startTile;
	}

	
	/**
	 * Gets the end tile of this move.
	 *
	 * @return the end tile of this move.
	 */
	public Coordinate getEndTile() {
		return this.endTile;
	}

	
	/**
	 * Gets the flag of this move.
	 *
	 * @return the flag of this move.
	 */
	public Move.Flag getFlag() {
		return this.flag;
	}


	/**
	 * Determines if this move resulted in the promotion of a piece. Promotion is defined if 
	 * {@code getFlag().equals(Move.Flag.PROMOTE_*)}
	 *
	 * @return true if this move resulted in the promotion of a piece.
	 */
	public boolean isPromotion() {
		if (this.flag.equals(Move.Flag.PROMOTE_KNIGHT) ||
			this.flag.equals(Move.Flag.PROMOTE_BISHOP) ||
			this.flag.equals(Move.Flag.PROMOTE_ROOK) ||
			this.flag.equals(Move.Flag.PROMOTE_QUEEN))
			return true;
		return false;
	}


	/**
	 * Determines if this move is an en passant move. Equivalent to 
	 * {@code getMove().equals(Move.Flag.EN_PASSANT)}.
	 *
	 * @return true if this move is an en passant move.
	 */
	public boolean isEnPassant() {
		return this.flag.equals(Move.Flag.EN_PASSANT);
	}


	/**
	 * Determines if this move resulted in a pawn moving two tiles forward. Equivalent to 
	 * {@code getMove().equals(Move.Flag.PAWN_TWO_FORWARD)}.
	 *
	 * @return true if this move resulted in a pawn moving two tiles forward.
	 */
	public boolean isPawnTwoForward() {
		return this.flag.equals(Move.Flag.PAWN_TWO_FORWARD);
	}


	/**
	 * Determines if this move resulted in kingside castling. Equivalent to 
	 * {@code getMove().equals(Move.Flag.CASTLE_KINGSIDE)}.
	 *
	 * @return true if this move resulted in kingside castling.
	 */
	public boolean isCastleKingside() {
		return this.flag.equals(Move.Flag.CASTLE_KINGSIDE);
	}


	/**
	 * Determines if this move resulted in queenside castling. Equivalent to 
	 * {@code getMove().equals(Move.Flag.CASTLE_QUEENSIDE)}.
	 *
	 * @return true if this move resulted in queenside castling.
	 */
	public boolean isCastleQueenside() {
		return this.flag.equals(Move.Flag.CASTLE_QUEENSIDE);
	}


	/**
	 * Determines the flag that should be assigned to a move.
	 *
	 * @param piece          the piece being moved.
	 * @param startTile      the tile the piece started on.
	 * @param endTile        the tile the piece ended on.
	 * @param enPassantTile  the en passant tile if one is available, {@code null} otherwise.
	 *
	 * @return a {@code Move.Flag} enumerator type.
	 *
	 * @throws NullPointerException      if any argument is other than {@code enPassantTile}.
	 * @throws IllegalArgumentException  if any of the {@code Coordinate} arguments are invalid.
	 */
	public static Move.Flag inferFlag(Piece piece,
									  Coordinate startTile, Coordinate endTile,
									  Coordinate enPassantTile) {
		if (piece == null || startTile == null || endTile == null)
			throw new NullPointerException("null arguments found: piece=" + piece + ", startTile=" +
										   startTile + ", endTile=" + endTile);
		if (!startTile.isValidTile() || !endTile.isValidTile())
			throw new IllegalArgumentException("invalid tiles found: startTile: " +
											   startTile.isValidTile() + ", endTile: " +
											   endTile.isValidTile());
		if (enPassantTile != null && !enPassantTile.isValidTile())
			throw new IllegalArgumentException("invalid EP tile: " + enPassantTile);
		
		// Validity check for the data. If a flag cannot be determined because any of the
		// arguments are invalid, then return NONE as the flag
		if (piece == null || startTile == null || endTile == null ||
			!startTile.isValidTile() || !endTile.isValidTile())
			return Flag.NONE;

		// Pawn flags: promotion, two forward, and ep capture
		if (piece.getType() == Piece.Type.PAWN) {
			// As with other places in the codebase, things like the pawn home row/promotion row,
			// and pawn direction change based on who the player is, so that needs to be found,
			// then the list of tiles need to be gatehred
			int promotionRowY = piece.isWhite() ? 7 : 0;
			List<Coordinate> promotionRowTiles = new ArrayList<>();
			for (int x = 0; x < 8; x++)
				promotionRowTiles.add(new Coordinate(x, promotionRowY));
			int yOffset = piece.isWhite() ? 1 : -1; // Which direction pawns move

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
				JOptionPane.showMessageDialog(null,
											  promotionComboBox,
											  "Promote to...",
											  JOptionPane.PLAIN_MESSAGE);
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
		else if (piece.getType().equals(Piece.Type.KING)) {
			if (startTile.shift(new Vector(2, 0)).equals(endTile))
				return Flag.CASTLE_KINGSIDE;

			else if (startTile.shift(new Vector(-2, 0)).equals(endTile))
				return Flag.CASTLE_QUEENSIDE;
		}

		return Flag.NONE;
	}


	/**
	 * Checks for equality between this {@code Move} object and another object. 
	 * Equality is determined by:
	 * <ul>
	 * <li> Equal start tiles
	 * <li> Equal end tiles
	 * <li> Equal flags
	 * </ul>
	 * <p>
	 * Equality of the components of the {@code Move} object are determined by the {@code equals} 
	 * methods of the components.
	 *
	 * @param obj  the object to compare equality.
	 *
	 * @return true if the objects are equal.
	 */
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
			mObj.getFlag().equals(this.flag))
			return true;

		return false;
	}


	/**
	 * Returns a string representation of this {@code Move} object. The string is composed of:
	 * <ul>
	 * <li> The start tile coordinate {@code toString}.
	 * <li> An arrow "->" between the moves.
	 * <li> The end tile coordinate {@code toString}.
	 * <li> The flag of the move.
	 * </ul>
	 * <p>
	 * An example of a move from A1 to B1 would be:
	 * <p>
	 * {@code a1b1 (NONE)}
	 *
	 * @return a string representation of this {@code Move} object.
	 */
	@Override
	public String toString() {
		return this.startTile + "" + this.endTile + " (" + this.flag + ")";
	}

}
