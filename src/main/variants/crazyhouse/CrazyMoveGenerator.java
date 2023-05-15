package variants.crazyhouse;


import engine.move.Move;
import engine.move.MoveGenerator;
import engine.board.Board;
import engine.board.BoardInfo;
import engine.piece.Piece;
import engine.util.Coordinate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


public class CrazyMoveGenerator {

	public static List<Move> generateLegalMoves(BoardInfo boardInfo,
												Map<Piece.Type, Integer> myPieces)
	{
		List<Move> legalMoves = new ArrayList<>();
		List<Move> pseudoLegalMoves = CrazyMoveGenerator.generatePseudoLegalMoves(boardInfo,
																				  myPieces);

		Piece.Color playerColor = boardInfo.whiteToMove ? Piece.Color.WHITE : Piece.Color.BLACK;
		Piece kingPiece = new Piece(Piece.Type.KING, playerColor);

		for (Move pseudoLegalMove : pseudoLegalMoves) {
			Coordinate startTile = pseudoLegalMove.getStartTile();
			Coordinate endTile = pseudoLegalMove.getEndTile();

			// Get the piece type being placed
			Piece.Type pieceType = Piece.Type.NONE;
			switch (startTile.getY()) {
			case 3 -> pieceType = Piece.Type.PAWN;
			case 4 -> pieceType = Piece.Type.KNIGHT;
			case 5 -> pieceType = Piece.Type.BISHOP;
			case 6 -> pieceType = Piece.Type.ROOK;
			case 7 -> pieceType = Piece.Type.QUEEN;
			}

			// Place the friendly player's piece for this move
		    BoardInfo ghostInfo = (BoardInfo) boardInfo.clone();
			ghostInfo.setPiece(endTile, new Piece(pieceType, playerColor));
			ghostInfo.updateAfterMove();

			// Create the board to test opponents moves on
			Board ghostBoard = new Board(ghostInfo);

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


	public static List<Move> generatePseudoLegalMoves(BoardInfo boardInfo,
												Map<Piece.Type, Integer> myPieces)
	{
		List<Move> pseudoLegalMoves = new ArrayList<>();
		Coordinate[] coordinates = Coordinate.getAllValidCoordinates();

		int promotionRowY = boardInfo.whiteToMove ? 7 : 0;

		for (Piece.Type pieceType : myPieces.keySet()) {
			int count = myPieces.get(pieceType);
			if (count <= 0)
				continue;

			int pieceRow = -1;
			switch (pieceType) {
			case PAWN -> pieceRow = 3;
			case KNIGHT -> pieceRow = 4;
			case BISHOP -> pieceRow = 5;
			case ROOK -> pieceRow = 6;
			case QUEEN -> pieceRow = 7;
			}

			for (Coordinate coordinate : coordinates) {
				// Prevent placing pawns on promotion row
				if (coordinate.getY() == promotionRowY && pieceType.equals(Piece.Type.PAWN))
					continue;

				// Prevent placing on existing pieces
				Piece existingPiece = boardInfo.getPiece(coordinate);
				if (existingPiece != null)
					continue;

				// Add a new move. The x coordinate of the start tile is always -1, the
				// y coordinate (row) corresponds to the row the piece is displayed
				// on to the end-user in the GUI
				Move move = new Move(new Coordinate(-1, pieceRow), coordinate, Move.Flag.NONE);
				pseudoLegalMoves.add(move);
			}
		}
		
		return pseudoLegalMoves;
	}

}
