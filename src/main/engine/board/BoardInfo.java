package engine.board;


import util.Coordinate;
import engine.fen.FenUtility;
import engine.piece.Piece;
import engine.move.Move;
import engine.move.MoveGenerator;
import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;


/**
 * Represents a chess board state using Forsyth–Edwards Notation. This class implements the
 * {@code Serializable} interface to allow the deep-copy functionality of the {@code clone} method.
 * <p>
 * This class provides several useful methods to check various conditions regarding the board 
 * state, however all fields are public. The intent of this class is to be used as a 
 * {@code struct} or record by a {@code Board} object, where the {@code BoardInfo} instance 
 * variable should always be private.
 *
 * @author Jonathan Uhler
 */
public class BoardInfo implements Serializable {

	/**
	 * Represents the current state of the board.
	 */
	public static enum State {
		/** An game that is still being played. */
		ONGOING,
		/** A game won by the white player. */
		WIN_WHITE,
		/** A game won by the black player. */
		WIN_BLACK,
		/** A game drawn through stalemate. */
		DRAW_STALEMATE,
		/** A game drawn by the 50-move rule. */
		DRAW_FIFTY_MOVE,
		/** A game drawn by threefold-repetition. */
		DRAW_REPETITION,
		/** A game drawn by insufficient material to checkmate. */
		DRAW_MATERIAL // Unused as this engine cannot calculate insufficient material
	}
	

	/** The state of the board as a Forsyth–Edwards Notation (FEN) string. */
	public String fenString;
	/** The board. */
	private Piece[][] tiles; // Only private field, explained more in getPiece(Coordinate)

	/** Whether it is the white player's turn to move. */
	public boolean whiteToMove;

	/** Whether white castling on the kingside is allowed. */
	public boolean castleK;
	/** Whether white castling on the queenside is allowed. */
	public boolean castleQ;
	/** Whether black castling on the kingside is allowed. */
	public boolean castlek;
	/** Whether black castling on the queenside is allowed. */
	public boolean castleq;

	/**
	 * The tile, if one exists, onto which a pawn can move to capture en passant style. This is 
	 * not the tile on which the enemy pawn exists, but the tile immediately behind the enemy pawn 
	 * (where said pawn would be if it had only moved one tile forward instead of two).
	 */
	public Coordinate enPassantTile;
	/** The number of halfmoves since the last pawn move. Used to enforce the 50-move rule. */
	public int halfmoves;
	/** The running total of fullmoves made. Incremented after the black player's turn. */
	public int fullmoves;

	/** A mapping of all the FEN strings and the number of times they occured during the game. */
	public Map<String, Integer> threefoldRepetitionTracker;
	/** A list of tiles controlled by the enemy player. */
	public List<Coordinate> tilesOpponentControls;


	/**
	 * Constructs a new {@code BoardInfo} object from the information in a FEN string. This 
	 * constructor should only be used by {@code FenUtility.informationFromFen} because of the 
	 * complexity of the arguments. When creating a new {@code BoardInfo} object, the fen utility 
	 * class should be used as a factory over the raw access of this construtor.
	 *
	 * @param tiles          a 2d array of pieces, representing the pieces on each tile of the 
	 *                       board. If there is not a piece on a given tile, that index is null.
	 * @param whiteToMove    whether it is the white player's turn to move.
	 * @param castleK        whether white castling on the kingside is allowed.
	 * @param castleQ        whether white castling on the queenside is allowed.
	 * @param castlek        whether black castling on the kingside is allowed.
	 * @param castleq        whether black castling on the queenside is allowed.
	 * @param enPassantTile  the tile for en passant, or {@code null} if no such tile exists.
	 * @param halfmoves      the numer of half moves (incremented after every turn, reset to 
	 *                       0 after a pawn moves).
	 * @param fullmoves      the number of full moves (incremented after the black player's turn).
	 *
	 * @throws NullPointerException      if {@code tiles == null}.
	 * @throws IllegalArgumentException  if {@code tiles} is not an 8x8 array.
	 * @throws IllegalArgumentException  if {@code halfmoves < 0}.
	 * @throws IllegalArgumentException  if {@code fullmoves < 0}.
	 */
	public BoardInfo(Piece[][] tiles, boolean whiteToMove,
					 boolean castleK, boolean castleQ, boolean castlek, boolean castleq,
					 Coordinate enPassantTile, int halfmoves, int fullmoves)
	{
		if (tiles == null)
			throw new NullPointerException("tiles was null");
		if (tiles.length != 8 || tiles[0].length != 8)
			throw new IllegalArgumentException("tiles is not an 8x8 array");
		if (halfmoves < 0)
			throw new IllegalArgumentException("halfmoves was negative: " + halfmoves);
		if (fullmoves < 0)
			throw new IllegalArgumentException("fullmoves was negative: " + fullmoves);
		
		this.tiles = tiles;
		
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


	/**
	 * Retrieves a piece from the board given a valid coordinate on the board. The {@code tiles} 
	 * field of this class, which is accessed by this method, if the only private field. This is 
	 * done to avoid confusion with row vs column major ordering. This method makes getting pieces 
	 * easier through the {@code Coordinate} class.
	 * <p>
	 * The X/Y coordinate system (used by the {@code Coordinate} class, where the chess board 
	 * represents quadrant I on the cartesian plane with the origin (0, 0) at A1) is in 
	 * column-major order.
	 * <p>
	 * The access into the {@code tiles} array is in row-major order. The correct access for a 
	 * piece on the tile A4 (x=0, y=3) for example would be:
	 * <p>
	 * {@code tiles[3][0]}
	 *
	 * @param c  the coordinate of the requested piece.
	 *
	 * @return the piece on the requested tile. If the tile is valid but no piece exists, 
	 *         {@code null} is returned.
	 *
	 * @throws IllegalArgumentException  if {@code c == null} or {@code !(c.isValidTile())}.
	 *
	 * @see util.Coordinate
	 */
	public Piece getPiece(Coordinate c) {
		if (c == null || !c.isValidTile())
			throw new IllegalArgumentException("c is null or invalid: " + c);
		return this.tiles[c.getY()][c.getX()];
	}


	/**
	 * Sets the piece on a given tile. The method exists instead of allowing direct access to the 
	 * {@code tiles} array for the reasons described in the {@code getPiece} documentation. The 
	 * piece will always be placed on the given tile (overriding existing pieces if needed) as 
	 * long as the coordinate is valid. The argument {@code p} is allowed to be {@code null} which 
	 * effectively clears the piece on the specified tile.
	 *
	 * @param c  the coordinate of the requested piece.
	 * @param p  the piece to place.
	 *
	 * @throws IllegalArgumentException  if {@code c == null} or {@code !(c.isValidTile())}.
	 */
	public void setPiece(Coordinate c, Piece p) {
		if (c == null || !c.isValidTile())
			throw new IllegalArgumentException("c is null or invalid: " + c);
		this.tiles[c.getY()][c.getX()] = p;
	}


	/**
	 * Returns a list of {@code Coordinate} objects pointing to the tiles containing a piece of 
	 * the same kind and color as the argument. Piece equality is checked using the 
	 * {@code Piece::equals} method.
	 *
	 * @param piece  an example piece of the same the variety (type and color) to find on the board.
	 *
	 * @return a list of {@code Coordinate} objects. If the argument {@code piece} is null, an 
	 *         empty list is returned. An empty list may also be returned if 
	 *         {@code piece.equals(pieceOnTile) == false} for all pieces in the {@code tiles} 
	 *         array.
	 *
	 * @see engine.piece.Piece
	 */
	public List<Coordinate> getTilesWithPiece(Piece piece) {
		List<Coordinate> tilesWithPiece = new ArrayList<>();
		if (piece == null)
			return tilesWithPiece;
		
		for (Coordinate c : Coordinate.getAllValidCoordinates()) {
			Piece pieceOnTile = this.getPiece(c);
			// Search through every valid coordinate and use the equals() method of the piece
			// object to check equality quickly
			if (pieceOnTile != null && pieceOnTile.equals(piece))
				tilesWithPiece.add(c);
		}
		
		return tilesWithPiece;
	}


	/**
	 * Determines the state of the game. This method does not modify any properties of this 
	 * {@code BoardInfo} object, it only accesses them.
	 *
	 * @return a property of the {@code BoardInfo.State} enumerator.
	 *
	 * @see engine.board.BoardInfo.State
	 */
	public BoardInfo.State inferState() {
		// Get information about the tile the king is on. If there is not exactly 1 king piece
		// for the current player then no state can be inferred, so assume the game is ongoing
		Piece.Color kingColor = (this.whiteToMove) ? Piece.Color.WHITE : Piece.Color.BLACK;
		Piece kingPiece = new Piece(Piece.Type.KING, kingColor);
		List<Coordinate> kingTiles = this.getTilesWithPiece(kingPiece);
		if (kingTiles.size() != 1)
			return BoardInfo.State.ONGOING;

		// Get the information needed to determine the state of the game:
		//  - The number of legal moves for the current player, to determine check vs checkmate,
		//    and for stalemate
		//  - The tile the king is on, to determine if the king is under attack
		//  - Whether the king is in check
		int numLegalMoves = MoveGenerator.generateLegalMoves(this).size();
		Coordinate kingTile = kingTiles.get(0);
		boolean inCheck = this.tilesOpponentControls.contains(kingTile);

		// Combinations of states:
		//  - Win for black: the white player has no legal moves and is in check
		//  - Win for white: the black player has no legal moves and is in check
		//  - Stalemate: the current player has no legal moves but is NOT in check (doesn't matter
		//    which player's turn it is, as the final state of the game is a draw)
		//  - 50 move rule: the number of halfmoves since the last pawn move or piece
		//    capture is >= 50
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


	/**
	 * Updates the properties of this object after a move has been made. 
	 * <p>
	 * This "update" is defined through some common changes after each move:
	 * <ul>
	 * <li> Updates the number of fullmoves after the black player's turn.
	 * <li> Changes the player.
	 * <li> Calls the more generic {@code update} method.
	 * </ul>
	 * <p>
	 * The second {@code update} method exists and is called by this method because the operations
	 * performed by {@code update} are also done upon the construction of a {@code BoardInfo} 
	 * object, while the operations within this method are only performed after a move has 
	 * been made.
	 */
	public void updateAfterMove() {
		// Update fullmoves
		if (!this.whiteToMove)
			this.fullmoves++;
		
		// Update turn
		this.whiteToMove = !this.whiteToMove;

		// Do the rest of the updates, which are also done in the constructor
		this.update();
	}


	/**
	 * Updates internal data structures. To update move-related properties (e.g. current player), 
	 * call {@code updateAfterMove} which also calls this routine.
	 * <p>
	 * This method updates the following:
	 * <ul>
	 * <li> The list of tiles controlled by the opponent player
	 * <li> The FEN string
	 * <li> The threefold repetition tracker
	 * </ul>
	 */
	private void update() {
		// Update tilesOpponentControls
		BoardInfo opponentInfo = (BoardInfo) this.clone();
		opponentInfo.whiteToMove = !this.whiteToMove;
	    this.tilesOpponentControls = MoveGenerator.generateTilesControlled(opponentInfo);

		// Update fen string
		this.fenString = FenUtility.fenFromInformation(this);

		// Update threefold repetition
		// Position part (first element) of fen string
		String posString = this.fenString.split(" ")[0];
		if (this.threefoldRepetitionTracker.containsKey(posString)) {
			int numOccurences = this.threefoldRepetitionTracker.get(posString);
			this.threefoldRepetitionTracker.put(posString, numOccurences + 1);
		}
		else
			this.threefoldRepetitionTracker.put(posString, 1);
	}


	/**
	 * Returns a deep-copy of this {@code BoardInfo} object.
	 *
	 * @return a deep-copy of this {@code BoardInfo} object. The returned type is {@code Object}, 
	 *         but is guaranteed to be castable to {@code BoardInfo} if not null. Upon any error, 
	 *         {@code null} is returned.
	 */
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


	/**
	 * Returns a string representation of this {@code BoardInfo} object.
	 * <p>
	 * The returned string is composed of:
	 * <ul>
	 * <li> The FEN string of this object.
	 * <li> The 8x8 grid, from the white player's perspective (A1 as the bottom-left tile). This 
	 *      grid contains the "." character for any blank tile, and the letters "PNBRQK" for the 
	 *      different pieces, as used in FEN notation. Lowercase letters represent black pieces, 
	 *      uppercase letters represent white pieces.
	 * </ul>
	 * <p>
	 * An example of this string for the starting position of a chess game is:
	 * <p>
	 * {@code rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1} <br>
	 * {@code r n b q k b n r} <br>
	 * {@code p p p p p p p p} <br>
	 * {@code . . . . . . . .} <br>
	 * {@code . . . . . . . .} <br>
	 * {@code . . . . . . . .} <br>
	 * {@code . . . . . . . .} <br>
	 * {@code P P P P P P P P} <br>
	 * {@code R N B Q K B N R}
	 */
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

}
