package engine.move;


import engine.util.Vector;
import engine.util.Coordinate;
import engine.piece.Piece;
import engine.board.BoardInfo;
import engine.board.Board;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;


/**
 * Generates a {@code List} of legal moves for a {@code BoardInfo} object.
 *
 * @author Jonathan Uhler
 */
public class MoveGenerator {

	// Define data constants for the offsets used by each of the pieces. Pawns are not included
	// here because they do not have any consistent offsets
	/** Knight movement offsets. */
	public static final Vector[] KNIGHT_OFFSETS = {new Vector(1, 2), new Vector(2, 1),
												   new Vector(2, -1), new Vector(1, -2),
												   new Vector(-1, -2), new Vector(-2, -1),
												   new Vector(-2, 1), new Vector(-1, 2)};
	/** Bishop movement offsets. */
	public static final Vector[] BISHOP_OFFSETS = {new Vector(1, 1),  new Vector(1, -1),
												   new Vector(-1, -1), new Vector(-1, 1)};
	/** Rook movement offsets. */
	public static final Vector[] ROOK_OFFSETS = {new Vector(0, 1), new Vector(1, 0),
												 new Vector(0, -1), new Vector(-1, 0)};
	/** Queen movement offsets. */
	public static final Vector[] QUEEN_OFFSETS = {new Vector(0, 1), new Vector(1, 1),
												  new Vector(1, 0), new Vector(1, -1),
												  new Vector(0, -1), new Vector(-1, -1),
												  new Vector(-1, 0), new Vector(-1, 1)};
	/** King movement offsets. */
	public static final Vector[] KING_OFFSETS = {new Vector(0, 1), new Vector(1, 1),
												 new Vector(1, 0), new Vector(1, -1),
												 new Vector(0, -1), new Vector(-1, -1),
												 new Vector(-1, 0), new Vector(-1, 1)};


	/**
	 * Generates a list of tiles controlled by the current player. The "tiles controlled" are 
	 * defined as the ending tiles of all the pseudo-legal moves the current player can make.
	 *
	 * @param boardInfo  a {@code BoardInfo} object that defines the board state.
	 *
	 * @return a {@code List} of {@code Coordinate} objects representing all the tiles controlled
	 *         by the current player.
	 */
	public static List<Coordinate> generateTilesControlled(BoardInfo boardInfo) {
		if (boardInfo == null)
			throw new NullPointerException("boardInfo was null");
		
		List<Coordinate> tilesControlled = new ArrayList<>();
		List<Move> moves = new ArrayList<>();

		// For each tile on the board, if there is a piece on that tile and it is a friendly piece,
		// then add all pseudo-legal moves that can be made by that piece to the "moves" list
		for (Coordinate tile : Coordinate.getAllValidCoordinates()) {
			Piece piece = boardInfo.getPiece(tile);

			if (piece != null) {
				if (piece.friendly(boardInfo.whiteToMove)) {
					switch (piece.getType()) {
					case PAWN:
						tilesControlled.addAll(MoveGenerator
											   .generatePawnTilesControlled(tile, boardInfo));
						break;
					case KNIGHT:
						moves.addAll(MoveGenerator.generateKnightMoves(tile, boardInfo));
						break;
					case BISHOP:
						moves.addAll(MoveGenerator
									 .generateSlidingMoves(tile, boardInfo,
														  MoveGenerator.BISHOP_OFFSETS));
						break;
					case ROOK:
						moves.addAll(MoveGenerator
									 .generateSlidingMoves(tile, boardInfo,
														  MoveGenerator.ROOK_OFFSETS));
						break;
					case QUEEN:
						moves.addAll(MoveGenerator
									 .generateSlidingMoves(tile, boardInfo,
														  MoveGenerator.QUEEN_OFFSETS));
						break;
					case KING:
						moves.addAll(MoveGenerator.generateKingMoves(tile, boardInfo));
						break;
					}
				}
			}
		}

		// Add only the ending tiles of the moves (tiles controlled) to the list of tiles
		// controlled, then return
		for (Move m : moves)
			tilesControlled.add(m.getEndTile());

		return tilesControlled;
	}
	

	/**
	 * Generates a {@code List} of strictly legal moves that can be made by the current player.
	 *
	 * @param boardInfo  a {@code BoardInfo} object that defines the board state.
	 *
	 * @return a {@code List} of strictly legal moves that can be made by the current player.
	 */
	public static List<Move> generateLegalMoves(BoardInfo boardInfo) {
		if (boardInfo == null)
			throw new NullPointerException("boardInfo was null");
		
		List<Move> legalMoves = new ArrayList<>();
		List<Move> pseudoLegalMoves = MoveGenerator.generatePseudoLegalMoves(boardInfo);

		// Based on whose turn it is, define the friendly player's king peice
		Piece.Color kingColor = (boardInfo.whiteToMove) ? Piece.Color.WHITE : Piece.Color.BLACK;
		Piece kingPiece = new Piece(Piece.Type.KING, kingColor);

		// Loop through all the pseudo legal moves generated for this player
		for (Move pseudoLegalMove : pseudoLegalMoves) {
			// Create a deep copy of the board information, and use that to create a "ghost"
			// board that moves can be played on to test their legality
			BoardInfo ghostInfo = (BoardInfo) boardInfo.clone();
			Board ghostBoard = new Board(ghostInfo);

			ghostBoard.makeMove(pseudoLegalMove);

			// Get the tile with the friendly king on it. The kingPiece created earlier is used
			// here. If there is no exactly 1 king piece, then legal moves cannot be generated,
			// so just return the list of pseudo legal moves
			List<Coordinate> kingTiles = ghostInfo.getTilesWithPiece(kingPiece);
			if (kingTiles.size() != 1)
				return pseudoLegalMoves;
			
			Coordinate kingTile = ghostInfo.getTilesWithPiece(kingPiece).get(0);

			// Set the current turn of the "ghost" board to the current opponent (remember that
			// boardInfo is the CURRENT state of the board and ghostInfo at this point must be in
			// the view of the current opponent). Because the BoardInfo.updateAfterMove() method
			// is called in Board.makeMove(), this should already be true, but setting it
			// explicitly makes sure of that
			ghostInfo.whiteToMove = !boardInfo.whiteToMove;

			// Generate the list of tiles the opponent controls. Any move that ends with the king
			// on a tile controlled by the opponent (the king being captured) was illegal and is
			// not added to the list of legal moves
			List<Coordinate> opponentAttackingTiles =
				MoveGenerator.generateTilesControlled(ghostInfo);
			
			if (!opponentAttackingTiles.contains(kingTile))
				legalMoves.add(pseudoLegalMove);
		}

		return legalMoves;
	}
	

	/**
	 * Generates a {@code List} of pseudo-legal moves that can be made by the current player. 
	 * A pseudo-legal move is defined as any move that adheres to the regular movement rules of a 
	 * given piece, but ignores any possible check state of the current player's king.
	 *
	 * @param boardInfo  a {@code BoardInfo} object that defines the board state.
	 *
	 * @return a {@code List} of pseudo-legal moves that can be made by the current player.
	 */
	public static List<Move> generatePseudoLegalMoves(BoardInfo boardInfo) {
		if (boardInfo == null)
			throw new NullPointerException("boardInfo was null");
		
		List<Move> pseudoLegalMoves = new ArrayList<>();

		// For each tile with a friendly piece, generate moves using the appropriate function
		// for that piece type
		for (Coordinate tile : Coordinate.getAllValidCoordinates()) {
			Piece piece = boardInfo.getPiece(tile);

			if (piece != null) {
				if (piece.friendly(boardInfo.whiteToMove)) {
					switch (piece.getType()) {
					case PAWN:
						pseudoLegalMoves.addAll(MoveGenerator.generatePawnMoves(tile, boardInfo));
						break;
					case KNIGHT:
						pseudoLegalMoves.addAll(MoveGenerator.generateKnightMoves(tile, boardInfo));
						break;
					case BISHOP:
						pseudoLegalMoves.addAll(MoveGenerator
												.generateSlidingMoves(tile, boardInfo,
																	 MoveGenerator.BISHOP_OFFSETS));
						break;
					case ROOK:
						pseudoLegalMoves.addAll(MoveGenerator
												.generateSlidingMoves(tile, boardInfo,
																	  MoveGenerator.ROOK_OFFSETS));
						break;
					case QUEEN:
						pseudoLegalMoves.addAll(MoveGenerator
												.generateSlidingMoves(tile, boardInfo,
																	  MoveGenerator.QUEEN_OFFSETS));
						break;
					case KING:
						pseudoLegalMoves.addAll(MoveGenerator.generateKingMoves(tile, boardInfo));
						break;
					}
				}
			}
		}

		return pseudoLegalMoves;
	}


	/**
	 * Generates moves for regular sliding pieces (pieces that move with a specific set of offset 
	 * vectors that can be scaled until the end of the board is reached): queen, bishop, and rook.
	 *
	 * @param startTile  the tile the piece starts on.
	 * @param boardInfo  a {@code BoardInfo} object that defines the board state.
	 * @param offsets    a list of offset vectors for how the sliding piece can move.
	 *
	 * @return a {@code List} of pseudo-legal moves.
	 */
	private static List<Move> generateSlidingMoves(Coordinate startTile,
												   BoardInfo boardInfo,
												   Vector[] offsets)
	{
		List<Move> movesGenerated = new ArrayList<>();

		for (Vector offset : offsets) {
			// tempOffset: the vector offset that is being compared in the while loop and
			// continuously being scaled. This is used for scaling instead of just offset from the
			// for loop because the scaling of just offset would compound as
			// 1x -> 2x -> 6x -> 24x ... when the desired effect is 1x -> 2x -> 3x -> 4x ...
			Vector tempOffset = offset;
			// scaleFactor: how much the move is scaled, up until the end of the board or when
			// hitting a piece, updated each time through the while loop
			int scaleFactor = 2;

			// Loop until the edge of the board
			while (startTile.shift(tempOffset).isValidTile()) {
				Coordinate endTile = startTile.shift(tempOffset);
				if (!endTile.isValidTile()) break;

				// Rules
				//  If there is no piece on the end tile, then add that as a move
				//  If there is a friendly piece, then you can move up to that but not onto or
				//  past it If there is a enemy piece, then you can move on to that (and capture it)
				//  but no past it
				
				Piece pieceOnEndTile = boardInfo.getPiece(endTile);
				if (pieceOnEndTile == null)
					movesGenerated.add(new Move(startTile, endTile));
				else {
					if (pieceOnEndTile.friendly(boardInfo.whiteToMove)) break;
					movesGenerated.add(new Move(startTile, endTile));
					if (!pieceOnEndTile.friendly(boardInfo.whiteToMove)) break;
				}

				// Scale the tempOffset used for calculations based on the value of the
				// original movement offset
				tempOffset = offset.scale(scaleFactor);
				scaleFactor++;
			}
		}
		
		return movesGenerated;
	}


	/**
	 * Generates moves for knights.
	 *
	 * @param startTile  the tile the piece starts on.
	 * @param boardInfo  a {@code BoardInfo} object that defines the board state.
	 *
	 * @return a {@code List} of pseudo-legal moves.
	 */
	private static List<Move> generateKnightMoves(Coordinate startTile, BoardInfo boardInfo) {
		List<Move> movesGenerated = new ArrayList<>();

		// Go through each of the knight offsets
		for (Vector offset : MoveGenerator.KNIGHT_OFFSETS) {
			Coordinate endTile = startTile.shift(offset);
			if (!endTile.isValidTile()) continue;

			// Rules
			//  If there is no piece on the end tile, then add that as a move
			//  If there is a friendly piece, then you can move up to that but not onto or past it
			//  If there is a enemy piece, then you can move on to that (and capture it)
			//  but not past it

			Piece pieceOnEndTile = boardInfo.getPiece(endTile);
			if (pieceOnEndTile == null)
				movesGenerated.add(new Move(startTile, endTile));
			else {
				if (pieceOnEndTile.friendly(boardInfo.whiteToMove)) continue;
				movesGenerated.add(new Move(startTile, endTile));
			}
		}
		
		return movesGenerated;
	}


	/**
	 * Generates moves for kings. These moves include castling.
	 *
	 * @param startTile  the tile the piece starts on.
	 * @param boardInfo  a {@code BoardInfo} object that defines the board state.
	 *
	 * @return a {@code List} of pseudo-legal moves.
	 */
	private static List<Move> generateKingMoves(Coordinate startTile, BoardInfo boardInfo) {
		List<Move> movesGenerated = new ArrayList<>();

		// Regular moves
		for (Vector offset : MoveGenerator.KING_OFFSETS) {
			Coordinate endTile = startTile.shift(offset);
			if (!endTile.isValidTile()) continue;

			// Rules
			//  If there is no piece on the end tile, then add that as a move
			//  If there is a friendly piece, then you can move up to that but not onto or past it
			//  If there is a enemy piece, then you can move on to that (and capture it) but
			//  not past it

			Piece pieceOnEndTile = boardInfo.getPiece(endTile);
			if (pieceOnEndTile == null)
				movesGenerated.add(new Move(startTile, endTile));
			else {
				if (pieceOnEndTile.friendly(boardInfo.whiteToMove)) continue;
				movesGenerated.add(new Move(startTile, endTile));
			}
		}

		// Castling moves
		if (boardInfo.whiteToMove)
			movesGenerated.addAll(MoveGenerator
								  .generateCastlingMoves(startTile, boardInfo,
														 boardInfo.castleK, boardInfo.castleQ));
		else
			movesGenerated.addAll(MoveGenerator
								  .generateCastlingMoves(startTile, boardInfo,
														 boardInfo.castlek, boardInfo.castleq));
		
		return movesGenerated;
	}


	/**
	 * Generates castling moves.
	 *
	 * @param startTile the tile the piece starts on.
	 * @param boardInfo a {@code BoardInfo} object that defines the board state.
	 *
	 * @return a {@code List} of pseudo-legal moves.
	 */
	private static List<Move> generateCastlingMoves(Coordinate startTile,
													BoardInfo boardInfo,
													boolean castleKingside,
													boolean castleQueenside)
	{
		List<Move> movesGenerated = new ArrayList<>();

		// Castling conditions (either side):
		//  - The king is not in check
		//  - Castling is allowed
		//  - The space between the king and rook is empty
		//  - The space between the king and rook is not controlled by the opponent

		if (!boardInfo.tilesOpponentControls.contains(startTile)) {
			Coordinate endTileK = startTile.shift(new Vector(2, 0));
			if (castleKingside == true &&
				boardInfo.getPiece(startTile.shift(new Vector(1, 0))) == null &&
				boardInfo.getPiece(startTile.shift(new Vector(2, 0))) == null &&
				!boardInfo.tilesOpponentControls.contains(startTile.shift(new Vector(1, 0))) &&
				!boardInfo.tilesOpponentControls.contains(startTile.shift(new Vector(2, 0))))
				movesGenerated.add(new Move(startTile, endTileK, Move.Flag.CASTLE_KINGSIDE));

			Coordinate endTileQ = startTile.shift(new Vector(-2, 0));
			if (castleQueenside == true &&
				boardInfo.getPiece(startTile.shift(new Vector(-1, 0))) == null &&
				boardInfo.getPiece(startTile.shift(new Vector(-2, 0))) == null &&
				boardInfo.getPiece(startTile.shift(new Vector(-3, 0))) == null &&
				!boardInfo.tilesOpponentControls.contains(startTile.shift(new Vector(-1, 0))) &&
				!boardInfo.tilesOpponentControls.contains(startTile.shift(new Vector(-2, 0))))
				movesGenerated.add(new Move(startTile, endTileQ, Move.Flag.CASTLE_QUEENSIDE));
		}

		return movesGenerated;
	}


	/**
	 * Generates a list of tiles controlled by pawns (because the tiles pawns control are not the 
	 * same as the tiles pawns can regularly move to). This method generates two moves at vectors 
	 * <-1, 1> and <1, 1> from a given player's perspective, even if no piece can be captured in 
	 * that square. Although, it should be noted the pawn capture tile will only be generated if 
	 * it is on the board (e.g. a pawn on the A or H file will only have 1 controlled tile).
	 *
	 * @param startTile  the tile the piece starts on.
	 * @param boardInfo  a {@code BoardInfo} object that defines the board state.
	 *
	 * @return a {@code List} of tiles controlled by a pawn on the given starting tile.
	 */
	private static List<Coordinate> generatePawnTilesControlled(Coordinate startTile,
																BoardInfo boardInfo)
	{
		List<Coordinate> tilesControlled = new ArrayList<>();

		 // Direction the pawn moves, always from the white perspective
		int pawnDir = (boardInfo.whiteToMove) ? 1 : -1;
		Vector v_leftCapture = new Vector(-1, 1 * pawnDir);
		Vector v_rightCapture = new Vector(1, 1 * pawnDir);
		Coordinate leftCapture = startTile.shift(v_leftCapture);
		Coordinate rightCapture = startTile.shift(v_rightCapture);

		if (leftCapture.isValidTile())
			tilesControlled.add(leftCapture);
		if (rightCapture.isValidTile())
			tilesControlled.add(rightCapture);
		
		return tilesControlled;
	}


	/**
	 * Generates moves for pawns. This includes regular 1-tile moves, starting 2-tile moves, 
	 * en passant, diagonal captures, and promotion.
	 * <p>
	 * The general algorithm for generating all pawn moves is:
	 * <ul>
	 * <li> let {@code DIR} = {@code boardInfo.whiteToMove ? 1 : -1}
	 * <li> generate {@code <0, 1*DIR>} IF on board AND empty
	 * <li> generate {@code <-1, 1*DIR>} IF on board AND enemy piece
	 * <li> generate {@code <1, 1*DIR>} IF on board AND enemy piece
	 * <li> generate PREVIOUS 3 MOVES with PROMOTE_TO_* IF (white AND end on a8-h8) OR 
	 *      (black AND end on a1-h1)
	 * <li> generate {@code <1, 1*DIR>} with EN_PASSANT IF {@code <1, 1*DIR>} IS ep_tile
	 * <li> generate {@code <-1, 1*DIR>} with EN_PASSANT IF {@code <-1, 1*DIR>} IS ep_tile
	 * <li> generate {@code <0, 2*DIR>} with TWO_FORWARD IF both empty
	 * </ul>
	 *
	 * @param startTile  the tile the piece starts on.
	 * @param boardInfo  a {@code BoardInfo} object that defines the board state.
	 *
	 * @return a {@code List} of pseudo-legal moves.
	 */
	private static List<Move> generatePawnMoves(Coordinate startTile, BoardInfo boardInfo) {
		List<Move> movesGenerated = new ArrayList<>();

		// Generate the vectors and coordinates for all of the possible positions of pawn moves
		int pawnDir = (boardInfo.whiteToMove) ? 1 : -1;
		int homeRowY = (boardInfo.whiteToMove) ? 1 : 6;
		int promotionRowY = (boardInfo.whiteToMove) ? 7 : 0;
		Vector v_oneForward = new Vector(0, 1 * pawnDir);
		Vector v_twoFoward = new Vector(0, 2 * pawnDir);
		Vector v_leftCapture = new Vector(-1, 1 * pawnDir);
		Vector v_rightCapture = new Vector(1, 1 * pawnDir);
		Coordinate oneForward = startTile.shift(v_oneForward);
		Coordinate twoForward = startTile.shift(v_twoFoward);
		Coordinate leftCapture = startTile.shift(v_leftCapture);
		Coordinate rightCapture = startTile.shift(v_rightCapture);
		List<Coordinate> homeRowTiles = new ArrayList<>();
		List<Coordinate> promotionRowTiles = new ArrayList<>();

		// Generate the coordinates for the pawn home row and promotion row
		for (int x = 0; x < 8; x++)
			homeRowTiles.add(new Coordinate(x, homeRowY));
		for (int x = 0; x < 8; x++)
			promotionRowTiles.add(new Coordinate(x, promotionRowY));

		
		// One forward
		if (oneForward.isValidTile() &&
			boardInfo.getPiece(oneForward) == null)
		{
			if (promotionRowTiles.contains(oneForward)) {
				movesGenerated.add(new Move(startTile, oneForward, Move.Flag.PROMOTE_KNIGHT));
				movesGenerated.add(new Move(startTile, oneForward, Move.Flag.PROMOTE_BISHOP));
				movesGenerated.add(new Move(startTile, oneForward, Move.Flag.PROMOTE_ROOK));
				movesGenerated.add(new Move(startTile, oneForward, Move.Flag.PROMOTE_QUEEN));
			}
			else
				movesGenerated.add(new Move(startTile, oneForward));
		}

		// Left capture
		if (leftCapture.isValidTile() &&
			boardInfo.getPiece(leftCapture) != null &&
			!boardInfo.getPiece(leftCapture).friendly(boardInfo.whiteToMove))
		{
			if (promotionRowTiles.contains(leftCapture)) {
				movesGenerated.add(new Move(startTile, leftCapture, Move.Flag.PROMOTE_KNIGHT));
				movesGenerated.add(new Move(startTile, leftCapture, Move.Flag.PROMOTE_BISHOP));
				movesGenerated.add(new Move(startTile, leftCapture, Move.Flag.PROMOTE_ROOK));
				movesGenerated.add(new Move(startTile, leftCapture, Move.Flag.PROMOTE_QUEEN));
			}
			else
				movesGenerated.add(new Move(startTile, leftCapture));
		}

		// Right capture
		if (rightCapture.isValidTile() &&
			boardInfo.getPiece(rightCapture) != null &&
			!boardInfo.getPiece(rightCapture).friendly(boardInfo.whiteToMove))
		{
			if (promotionRowTiles.contains(rightCapture)) {
				movesGenerated.add(new Move(startTile, rightCapture, Move.Flag.PROMOTE_KNIGHT));
				movesGenerated.add(new Move(startTile, rightCapture, Move.Flag.PROMOTE_BISHOP));
				movesGenerated.add(new Move(startTile, rightCapture, Move.Flag.PROMOTE_ROOK));
				movesGenerated.add(new Move(startTile, rightCapture, Move.Flag.PROMOTE_QUEEN));
			}
			else
				movesGenerated.add(new Move(startTile, rightCapture));
		}

		// Two forward
		if (homeRowTiles.contains(startTile) &&
			twoForward.isValidTile() &&
			boardInfo.getPiece(oneForward) == null &&
			boardInfo.getPiece(twoForward) == null)
			movesGenerated.add(new Move(startTile, twoForward, Move.Flag.PAWN_TWO_FORWARD));

		// En passant
		if (boardInfo.enPassantTile != null &&
			boardInfo.enPassantTile.isValidTile() &&
			(leftCapture.equals(boardInfo.enPassantTile) ||
			 rightCapture.equals(boardInfo.enPassantTile)))
			movesGenerated.add(new Move(startTile, boardInfo.enPassantTile, Move.Flag.EN_PASSANT));
		
		return movesGenerated;
	}

}
