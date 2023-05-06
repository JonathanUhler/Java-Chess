package engine.fen;


import util.Coordinate;
import util.StringUtility;
import engine.board.BoardInfo;
import engine.piece.Piece;


/**
 * Handles the parsing of Forsyth-Edwards Notation strings, which are the standard supported 
 * format of board information for this engine.
 *
 * @author Jonathan Uhler
 */
public class FenUtility {

	/**
	 * Creates a {@code BoardInfo} object from a FEN string.
	 *
	 * @param fenString  the FEN string to convert to a {@code BoardInfo} object.
	 *
	 * @return a {@code BoardInfo} object if one could be generated. If an object could not be 
	 *         generated and could not be salvaged an exception will be thrown.
	 *
	 * @throws NullPointerException      if {@code fenString == null}.
	 * @throws IllegalArgumentException  if the argument contains an incorrect number of components.
	 * @throws IllegalArgumentException  if the board dimensions of the FEN string are invalid 
	 *                                   (exceed 8x8).
	 * @throws IllegalArgumentException  if the player identifier is not {@code "w"} or {@code "b"}.
	 * @throws IllegalArgumentException  if the en passant tile identifier is not "-" and not two 
	 *                                   characters. If the en passant tile contains two characters
	 *                                   but is not a valid tile on the board, the en passant tile 
	 *                                   returned by this object is assumed to be none.
	 *
	 * @see engine.board.BoardInfo
	 */
	public static BoardInfo informationFromFen(String fenString) {
		if (fenString == null)
			throw new NullPointerException("fenString was null");
		
		String[] fenSplit = fenString.split(" ");
		if (fenSplit.length != 6)
			throw new IllegalArgumentException("fenString does not have length 6: " + fenString);

		// Tiles
		// Start tile for a FEN string (from white's perspective) is top-left first, bottom-right
		// last. But A1/(0,0) is at the bottom-left which is strangely not the last square in FEN.
		// To address this, every index is relative to the bottom-left square (A1) as "0" and thus
		// the first square for FEN is x=0,y=8
		String tilesStr = fenSplit[0];
		Piece[][] tiles = new Piece[8][8];
		int x = 0;
		int y = 7;

		for (char fenChar : tilesStr.toCharArray()) {
			// "/" for moving down to the next line
			if (fenChar == '/') {
				x = 0;
				y--;
			}
			// Numbers for blank spaces
			else if (Character.isDigit(fenChar)) {
				x += Character.getNumericValue(fenChar);
			}
			// Other characters, assumed to be valid letters, otherwise a null piece
			// (blank tile) will be placed
			else {
				Piece.Color color =
					(Character.isUpperCase(fenChar)) ?
					Piece.Color.WHITE :
					Piece.Color.BLACK;
				Piece.Type type = Piece.Type.NONE;
				
				switch (Character.toLowerCase(fenChar)) {
				case 'p':
					type = Piece.Type.PAWN;
					break;
				case 'n':
					type = Piece.Type.KNIGHT;
					break;
				case 'b':
					type = Piece.Type.BISHOP;
					break;
				case 'r':
					type = Piece.Type.ROOK;
					break;
				case 'q':
					type = Piece.Type.QUEEN;
					break;
				case 'k':
					type = Piece.Type.KING;
					break;
				}

				if (!(new Coordinate(x, y)).isValidTile())
					throw new IllegalArgumentException("invalid dims, found x=" + x +
													   ", y=" + y + ": " + fenString);

				tiles[y][x] = new Piece(type, color);
				x++;
			}
		}

		// Current move
		String playerStr = fenSplit[1];
		if (!playerStr.equals("w") && !playerStr.equals("b"))
			throw new IllegalArgumentException("unknown player identifier: " + playerStr);
		boolean whiteToMove = playerStr.equals("w");

		// Castling rights
		String castlingRightsStr = fenSplit[2];
		boolean castleK = castlingRightsStr.contains("K");
		boolean castleQ = castlingRightsStr.contains("Q");
		boolean castlek = castlingRightsStr.contains("k");
		boolean castleq = castlingRightsStr.contains("q");

		// En passant tile
		String enPassantStr = fenSplit[3];
		Coordinate enPassantTile;
		if (enPassantStr.equals("-"))
			enPassantTile = null;
		else {
			if (enPassantStr.length() != 2)
				throw new IllegalArgumentException("invalid EP tile id length: " + enPassantStr);
			
			char epCol = enPassantStr.charAt(0);
			int epX = epCol - 'a';
			int epY = Character.getNumericValue(enPassantStr.charAt(1)) - 1;
			enPassantTile = new Coordinate(epX, epY);
			if (!enPassantTile.isValidTile())
				enPassantTile = null;
		}

		// Halfmoves
		int halfmoves = Integer.parseInt(fenSplit[4]);

		// Fullmoves
		int fullmoves = Integer.parseInt(fenSplit[5]);

		// Assemble and return all components as a FenInfo object
		return new BoardInfo(tiles, whiteToMove,
							 castleK, castleQ, castlek, castleq,
							 enPassantTile, halfmoves, fullmoves);
	}


	/**
	 * Creates a FEN {@code String} object from a {@code BoardInfo} object.
	 *
	 * @param boardInfo  the {@code BoardInfo} object to convert to a FEN string.
	 *
	 * @return a {@code String} object.
	 *
	 * @throws NullPointerException  if {@code boardInfo == null}.
	 */
	public static String fenFromInformation(BoardInfo boardInfo) {
		if (boardInfo == null)
			throw new NullPointerException("boardInfo was null");
		
		String fenString = "";

		// Tiles
		for (int y = 7; y >= 0; y--) {
			int numEmptyCols = 0;
			for (int x = 0; x < 8; x++) {
				Piece piece = boardInfo.getPiece(new Coordinate(x, y));
				if (piece != null) {
					if (numEmptyCols != 0) {
						fenString += numEmptyCols;
						numEmptyCols = 0;
					}

					Piece.Type pieceType = piece.getType();
					String pieceChar = "";
					switch (pieceType) {
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

					if (piece.isBlack())
						pieceChar = pieceChar.toLowerCase();

					fenString += pieceChar;
				}
				else {
					numEmptyCols++;
				}
			}

			if (numEmptyCols != 0)
				fenString += numEmptyCols;
			if (y != 0)
				fenString += "/";
		}

		// Turn to play
		fenString += " ";
		if (boardInfo.whiteToMove)
			fenString += "w";
		else
			fenString += "b";

		// Castling rights
		String castlingRights = "";
		if (boardInfo.castleK)
			castlingRights += "K";
		if (boardInfo.castleQ)
			castlingRights += "Q";
		if (boardInfo.castlek)
			castlingRights += "k";
		if (boardInfo.castleq)
			castlingRights += "q";

		if (castlingRights.equals(""))
			castlingRights = "-";

		fenString += " ";
		fenString += castlingRights;

		// En passant tile
		Coordinate enPassantTile = boardInfo.enPassantTile;

		fenString += " ";
		if (enPassantTile != null)
			fenString += StringUtility.coordinateToString(enPassantTile);
		else
			fenString += "-";

		// Halfmoves (fifty move rule)
		fenString += " ";
		fenString += boardInfo.halfmoves;

		// Fullmoves
		fenString += " ";
		fenString += boardInfo.fullmoves;

		return fenString;
	}

}
