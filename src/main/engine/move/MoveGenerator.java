// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// MoveGenerator.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package engine.move;


import util.Log;
import util.Vector;
import util.Coordinate;
import engine.piece.Piece;
import engine.board.BoardInfo;
import engine.board.Board;
import java.util.ArrayList;
import java.util.Arrays;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class MoveGenerator
//
// Generates legal moves based on the current state of the board
//
public class MoveGenerator {

	// Define data constants for the offsets used by each of the pieces. Pawns are not included here
	// because they do not have any consistent offsets
	public static final Vector[] KNIGHT_OFFSETS = {new Vector(1, 2), new Vector(2, 1), new Vector(2, -1),
												   new Vector(1, -2), new Vector(-1, -2), new Vector(-2, -1),
												   new Vector(-2, 1), new Vector(-1, 2)};
	public static final Vector[] BISHOP_OFFSETS = {new Vector(1, 1),  new Vector(1, -1),
												   new Vector(-1, -1), new Vector(-1, 1)};
	public static final Vector[] ROOK_OFFSETS = {new Vector(0, 1), new Vector(1, 0),
												 new Vector(0, -1), new Vector(-1, 0)};
	public static final Vector[] QUEEN_OFFSETS = {new Vector(0, 1), new Vector(1, 1), new Vector(1, 0),
												  new Vector(1, -1), new Vector(0, -1), new Vector(-1, -1),
												  new Vector(-1, 0), new Vector(-1, 1)};
	public static final Vector[] KING_OFFSETS = {new Vector(0, 1), new Vector(1, 1), new Vector(1, 0),
												  new Vector(1, -1), new Vector(0, -1), new Vector(-1, -1),
												  new Vector(-1, 0), new Vector(-1, 1)};


	// ====================================================================================================
	// public static ArrayList<Coordinate> generateTilesControlled
	//
	// Generates a list of tiles controlled by the CURRENT (friendly) player. Tiles controlled are all the
	// ending tiles of the PSEUDO-legal moves the player can make.
	//
	// Arguments--
	//
	//  boardInfo: a BoardInfo object that defines the board state, used to generate tiles
	//
	// Returns--
	//
	//  An ArrayList of Coordiantes representing all the tiles the current/friendly player has control of
	//
	public static ArrayList<Coordinate> generateTilesControlled(BoardInfo boardInfo) {
		ArrayList<Coordinate> tilesControlled = new ArrayList<>();
		ArrayList<Move> moves = new ArrayList<>();

		// For each tile on the board, if there is a piece on that tile and it is a friendly piece, then
		// add all pseudo-legal moves that can be made by that piece to the "moves" list
		for (Coordinate tile : Coordinate.getAllValidCoordinates()) {
			Piece piece = boardInfo.getPiece(tile);

			if (piece != null) {
				if (piece.friendly(boardInfo.whiteToMove)) {
					switch (piece.getType()) {
					case Piece.Type.PAWN:
						tilesControlled.addAll(MoveGenerator.generatePawnTilesControlled(tile, boardInfo));
						break;
					case Piece.Type.KNIGHT:
						moves.addAll(MoveGenerator.generateKnightMoves(tile, boardInfo));
						break;
					case Piece.Type.BISHOP:
						moves.addAll(MoveGenerator.generateSlidingMoves(tile, boardInfo, MoveGenerator.BISHOP_OFFSETS));
						break;
					case Piece.Type.ROOK:
						moves.addAll(MoveGenerator.generateSlidingMoves(tile, boardInfo, MoveGenerator.ROOK_OFFSETS));
						break;
					case Piece.Type.QUEEN:
						moves.addAll(MoveGenerator.generateSlidingMoves(tile, boardInfo, MoveGenerator.QUEEN_OFFSETS));
						break;
					case Piece.Type.KING:
						moves.addAll(MoveGenerator.generateKingMoves(tile, boardInfo));
						break;
					}
				}
			}
		}

		// Add only the ending tiles of the moves (tiles controlled) to the list of tiles controlled, then return
		for (Move m : moves)
			tilesControlled.add(m.getEndTile());

		return tilesControlled;
	}
	// end: public static ArrayList<Coordinate> generateTilesControlled
	

	// ====================================================================================================
	// public static ArrayList<Move> generateLegalMoves
	//
	// Generates a list of strictly legal moves that can be made by the current player
	//
	// Arguments--
	//
	//  boardInfo: information about the current state of the board
	//
	// Returns--
	//
	//  An ArrayList of legal moves
	//
	public static ArrayList<Move> generateLegalMoves(BoardInfo boardInfo) {
		ArrayList<Move> legalMoves = new ArrayList<>();
		ArrayList<Move> pseudoLegalMoves = MoveGenerator.generatePseudoLegalMoves(boardInfo);

		// Based on whose turn it is, define the friendly player's king peice
		int kingColor = (boardInfo.whiteToMove) ? Piece.Color.WHITE : Piece.Color.BLACK;
		Piece kingPiece = new Piece(Piece.Type.KING, kingColor);

		// Loop through all the pseudo legal moves generated for this player
		for (Move pseudoLegalMove : pseudoLegalMoves) {
			// Create a deep copy of the board information, and use that to create a "ghost" board that moves
			// can be played on to test their legality
			BoardInfo ghostInfo = (BoardInfo) boardInfo.clone();
			Board ghostBoard = new Board(ghostInfo);

			ghostBoard.makeMove(pseudoLegalMove);

			// Get the tile with the friendly king on it. The kingPiece created earlier is used here.
			// If there is no exactly 1 king piece, then legal moves cannot be generated, so just return
			// the list of pseudo legal moves
			ArrayList<Coordinate> kingTiles = ghostInfo.getTilesWithPiece(kingPiece);
			if (kingTiles.size() != 1) {
				Log.stdlog(Log.WARN, "MoveGenerator", "no king piece present to calculate legal moves");
				return pseudoLegalMoves;
			}
			Coordinate kingTile = ghostInfo.getTilesWithPiece(kingPiece).get(0);

			// Set the current turn of the "ghost" board to the current opponent (remember that boardInfo is the
			// CURRENT state of the board and ghostInfo at this point must be in the view of the current
			// opponent). Because the BoardInfo.updateAfterMove() method is called in Board.makeMove(), this
			// should already be true, but setting it explicitly makes sure of that
			ghostInfo.whiteToMove = !boardInfo.whiteToMove;

			// Generate the list of tiles the opponent controls. Any move that ends with the king on a tile
			// controlled by the opponent (the king being captured) was illegal and is not added to the list
			// of legal moves
			ArrayList<Coordinate> opponentAttackingTiles = MoveGenerator.generateTilesControlled(ghostInfo);
			if (!opponentAttackingTiles.contains(kingTile))
				legalMoves.add(pseudoLegalMove);
		}

		return legalMoves;
	}
	// end: public static ArrayList<Move> generateLegalMoves
	

	// ====================================================================================================
	// public static ArrayList<Move> generatePseudoLegalMoves
	//
	// Generates a list of pseudo legal moves: all moves that can be made given the pieces movement, but
	// INCLUDING any moves that could result in the capture of the king
	//
	// Arguments--
	//
	//  boardInfo: information about the state of the board
	//
	// Returns--
	//
	//  An ArrayList of all pseudo legal moves
	//
	public static ArrayList<Move> generatePseudoLegalMoves(BoardInfo boardInfo) {
		ArrayList<Move> pseudoLegalMoves = new ArrayList<>();

		// For each tile with a friendly piece, generate moves using the appropriate function for that piece type
		for (Coordinate tile : Coordinate.getAllValidCoordinates()) {
			Piece piece = boardInfo.getPiece(tile);

			if (piece != null) {
				if (piece.friendly(boardInfo.whiteToMove)) {
					switch (piece.getType()) {
					case Piece.Type.PAWN:
						pseudoLegalMoves.addAll(MoveGenerator.generatePawnMoves(tile, boardInfo));
						break;
					case Piece.Type.KNIGHT:
						pseudoLegalMoves.addAll(MoveGenerator.generateKnightMoves(tile, boardInfo));
						break;
					case Piece.Type.BISHOP:
						pseudoLegalMoves.addAll(MoveGenerator.generateSlidingMoves(tile, boardInfo,
																				   MoveGenerator.BISHOP_OFFSETS));
						break;
					case Piece.Type.ROOK:
						pseudoLegalMoves.addAll(MoveGenerator.generateSlidingMoves(tile, boardInfo,
																				   MoveGenerator.ROOK_OFFSETS));
						break;
					case Piece.Type.QUEEN:
						pseudoLegalMoves.addAll(MoveGenerator.generateSlidingMoves(tile, boardInfo,
																				   MoveGenerator.QUEEN_OFFSETS));
						break;
					case Piece.Type.KING:
						pseudoLegalMoves.addAll(MoveGenerator.generateKingMoves(tile, boardInfo));
						break;
					}
				}
			}
		}

		return pseudoLegalMoves;
	}
	// end: public static ArrayList<Move> generatePseudoLegalMoves


	// ====================================================================================================
	// private static ArrayList<Move> generateSlidingMoves
	//
	// Generates moves for regular sliding pieces (pieces that move with a specific set of offset vectors
	// that can be scalled until the end of the board is reached): queen, bishop, and rook
	//
	// Arguments--
	//
	//  startTile: the tile the piece starts on
	//
	//  boardInfo: information about the state of the board
	//
	//  offsets:   a list of offset vectors for how the sliding piece can move
	//
	// Returns--
	//
	//  An ArrayList of pseudo legal moves for the sliding piece specified based on the offsets list
	//
	private static ArrayList<Move> generateSlidingMoves(Coordinate startTile, BoardInfo boardInfo, Vector[] offsets) {
		ArrayList<Move> movesGenerated = new ArrayList<>();

		for (Vector offset : offsets) {
			// tempOffset: the vector offset that is being compared in the while loop and continuously being scaled.
			//             This is used for scaling instead of just offset from the for loop because the scaling
			//             of just offset would compound as 1x -> 2x -> 6x -> 24x ... when the desired effect is
			//             1x -> 2x -> 3x -> 4x ...
			Vector tempOffset = offset;
			// scaleFactor: how much the move is scaled, up until the end of the board or when hitting a piece,
			//              updated each time through the while loop
			int scaleFactor = 2;

			// Loop until the edge of the board
			while (startTile.shift(tempOffset).isValidTile()) {
				Coordinate endTile = startTile.shift(tempOffset);
				if (!endTile.isValidTile()) break;

				// Rules
				//  If there is no piece on the end tile, then add that as a move
				//  If there is a friendly piece, then you can move up to that but not onto or past it
				//  If there is a enemy piece, then you can move on to that (and capture it) but no past it
				
				Piece pieceOnEndTile = boardInfo.getPiece(endTile);
				if (pieceOnEndTile == null)
					movesGenerated.add(new Move(startTile, endTile));
				else {
					if (pieceOnEndTile.friendly(boardInfo.whiteToMove)) break;
					movesGenerated.add(new Move(startTile, endTile));
					if (!pieceOnEndTile.friendly(boardInfo.whiteToMove)) break;
				}

				// Scale the tempOffset used for calculations based on the value of the original movement offset
				tempOffset = offset.scale(scaleFactor);
				scaleFactor++;
			}
		}
		
		return movesGenerated;
	}
	// end: private static ArrayList<Move> generateSlidingMoves


	// ====================================================================================================
	// private static ArrayList<Move> generateKnightMoves
	//
	// Geneates a list of pseudo legal moves for a knight
	//
	// Arguments--
	//
	//  startTile: the tile the piece starts on
	//
	//  boardInfo: information about the state of the board
	//
	// Returns--
	//
	//  An ArrayList of pseudo legal moves for a knight
	//
	private static ArrayList<Move> generateKnightMoves(Coordinate startTile, BoardInfo boardInfo) {
		ArrayList<Move> movesGenerated = new ArrayList<>();

		// Go through each of the knight offsets
		for (Vector offset : MoveGenerator.KNIGHT_OFFSETS) {
			Coordinate endTile = startTile.shift(offset);
			if (!endTile.isValidTile()) continue;

			// Rules
			//  If there is no piece on the end tile, then add that as a move
			//  If there is a friendly piece, then you can move up to that but not onto or past it
			//  If there is a enemy piece, then you can move on to that (and capture it) but no past it

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
	// end: private static ArrayList<Move> generateKnightMoves


	// ====================================================================================================
	// private static ArrayList<Move> generateKingMoves
	//
	// Generates a list of pseudo legal moves for a king
	//
	// Arguments--
	//
	//  startTile: the tile the move starts on
	//
	//  boardInfo: information about the state of the board
	//
	// Returns--
	//
	//  An ArrayList of pseudo legal moves for a king
	//
	private static ArrayList<Move> generateKingMoves(Coordinate startTile, BoardInfo boardInfo) {
		ArrayList<Move> movesGenerated = new ArrayList<>();

		// Regular moves
		for (Vector offset : MoveGenerator.KING_OFFSETS) {
			Coordinate endTile = startTile.shift(offset);
			if (!endTile.isValidTile()) continue;

			// Rules
			//  If there is no piece on the end tile, then add that as a move
			//  If there is a friendly piece, then you can move up to that but not onto or past it
			//  If there is a enemy piece, then you can move on to that (and capture it) but no past it

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
			movesGenerated.addAll(MoveGenerator.generateCastlingMoves(startTile, boardInfo,
																	  boardInfo.castleK, boardInfo.castleQ));
		else
			movesGenerated.addAll(MoveGenerator.generateCastlingMoves(startTile, boardInfo,
																	  boardInfo.castlek, boardInfo.castleq));
		
		return movesGenerated;
	}
	// end: private static ArrayList<Move> generateKingMoves


	// ====================================================================================================
	// private static ArrayList<Move> generateCastlingMoves
	//
	// Generates fully legal castling moves
	//
	// Arguments--
	//
	//  startTile:       the tile the moves starts on
	//
	//  boardInfo:       information about the state of the board
	//
	//  castleKingside:  whether castling kingside for this player is allowed
	//
	//  castleQueenside: whether castling queenside for this player is allowed
	//
	// Returns--
	//
	//  An ArrayList of fully legal castling moves
	//
	private static ArrayList<Move> generateCastlingMoves(Coordinate startTile, BoardInfo boardInfo,
														 boolean castleKingside, boolean castleQueenside) {
		ArrayList<Move> movesGenerated = new ArrayList<>();

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
	// end: private static ArrayList<Move> generateCastlingMoves


	// ====================================================================================================
	// private static ArrayList<Coordinate> generatePawnTilesControlled
	//
	// Generates a list of tiles controlled by a pawn. This is separate from generatePawnMoves. This method
	// generates the two moves at vectors <-1, 1> and <1, 1> from a given players perspective, even if
	// no piece can be captured on that square. Although, it should be noted the pawn capture move will
	// only be generated if it is on the board (so a pawn on the a or h file will only have 1 controlled
	// tile)
	//
	// Arguments--
	//
	//  startTile: the tile the move starts on
	//
	//  boardInfo: information about the state of the board
	//
	// Returns--
	//
	//  An ArrayList of tiles controlled by a pawn
	//
	private static ArrayList<Coordinate> generatePawnTilesControlled(Coordinate startTile, BoardInfo boardInfo) {
		ArrayList<Coordinate> tilesControlled = new ArrayList<>();

		int pawnDir = (boardInfo.whiteToMove) ? 1 : -1; // Direction the pawn moves, always from the white perspective
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
	// end: private static ArrayList<Coordinate> generatePawnTilesControlled


	// ====================================================================================================
	// private static ArrayList<Move> generatePawnMoves
	//
	// Generates a list of pseudo legal moves for a pawn
	//
	// Pawn moves general rules and algorithm:
	//  DIR = (boardInfo.whiteToMove) ? 1 : -1;
	//  - <0,1*DIR> if valid AND no piece blocking <0,1*DIR>  \  if (white AND end on a8-h8) OR (black AND end on a1-h1)
	//  - <-1,1*DIR> if valid AND enemy piece                  | then add 4 moves with each possible promotion flag
	//  - <1,1*DIR> if valid AND enemy piece                  /
	//  - (<0,2*DIR> if valid AND no piece blocking <0,1*DIR> AND <0,2*DIR>) AND
	//    ((white AND on square a2-h2) OR (black AND on square a7-h7))
	//  - <-1,1*DIR> flag ep if <-1,1*DIR> == ep tile
	//  - <1,1*DIR> flag ep if <1,1*DIR> == ep tile
	//
	// Arguments--
	//
	//  startTile: the tile the move starts on
	//
	//  boardInfo: information about the state of the board
	//
	// Returns--
	//
	//  An ArrayList of pseudo legal moves for a pawn
	//
	private static ArrayList<Move> generatePawnMoves(Coordinate startTile, BoardInfo boardInfo) {
		ArrayList<Move> movesGenerated = new ArrayList<>();

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
		ArrayList<Coordinate> homeRowTiles = new ArrayList<>();
		ArrayList<Coordinate> promotionRowTiles = new ArrayList<>();

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
	// end: private static ArrayList<Move> generatePawnMoves

}
// end: public class MoveGenerator
