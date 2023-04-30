// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// FenUtility.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package engine.fen;


import util.Log;
import util.Coordinate;
import util.StringUtility;
import engine.board.BoardInfo;
import engine.piece.Piece;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class FenUtility
//
// Handles parsing of FEN strings, the only supported format of board information by this engine
//
public class FenUtility {

	// ====================================================================================================
	// public static BoardInfo informationFromFen
	//
	// Builds a BoardInfo object from a FEN string
	//
	// Arguments--
	//
	//  fenString: the FEN string to convert to a BoardInfo object
	//
	// Returns--
	//
	//  A BoardInfo object if one could be successfully generated. If a BoardInfo object could not be
	//  generated, a FATAL error is thrown
	//
	public static BoardInfo informationFromFen(String fenString) {
		String[] fenSplit = fenString.split(" ");

		if (fenSplit.length != 6) {
			Log.stdlog(Log.ERROR, "FenUtility", "fen string with invalid length passed to informationFromFen");
			return null;
		}

		// Tiles
		// Start tile for a FEN string (from white's perspective) is top-left first, bottom-right last. But A1/(0,0) is
		// at the bottom-left which is strangely not the last square in FEN. To address this, every index is relative
		// to the bottom-left square (A1) as "0" and thus the first square for FEN is x=0,y=8
		Piece[][] tiles = new Piece[8][8];
		int x = 0;
		int y = 7;

		for (char fenChar : fenSplit[0].toCharArray()) {
			// "/" for moving down to the next line
			if (fenChar == '/') {
				x = 0;
				y--;
			}
			// Numbers for blank spaces
			else if (Character.isDigit(fenChar)) {
				x += Character.getNumericValue(fenChar);
			}
			// Other characters, assumed to be valid letters, otherwise a null piece (blank tile) will be placed
			else {
				int color = (Character.isUpperCase(fenChar)) ? Piece.Color.WHITE : Piece.Color.BLACK;
				int type = Piece.Type.NONE;
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
				default:
					Log.stdlog(Log.WARN, "FenUtility", "character in fen string was not in PNBRQK, defaulting to NONE");
					break;
				}

				if (!(new Coordinate(x, y)).isValidTile()) {
					Log.stdlog(Log.ERROR, "FenUtility", "invalid coordinate (invalid mix of numbers or \"/\")");
					return null;
				}

				tiles[y][x] = new Piece(type, color);
				x++;
			}
		}

		// Current move
		if (!fenSplit[1].equals("w") && !fenSplit[1].equals("b")) {
			Log.stdlog(Log.ERROR, "FenUtility", "invalid turn identifier, not w or b");
			return null;
		}
		
		boolean whiteToMove = fenSplit[1].equals("w");

		// Castling rights
		boolean castleK = fenSplit[2].contains("K");
		boolean castleQ = fenSplit[2].contains("Q");
		boolean castlek = fenSplit[2].contains("k");
		boolean castleq = fenSplit[2].contains("q");

		// En passant tile
		Coordinate enPassantTile;
		if (fenSplit[3].equals("-"))
			enPassantTile = null;
		else {
			char epCol = fenSplit[3].charAt(0);
			int epX = epCol - 'a';
			int epY = Character.getNumericValue(fenSplit[3].charAt(1)) - 1;
			enPassantTile = new Coordinate(epX, epY);
			if (!enPassantTile.isValidTile()) {
				Log.stdlog(Log.WARN, "FenUtility", "calculated en passant tile is invalid, defaulting to null");
				enPassantTile = null;
			}
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
	// end: public static BoardInfo informationFromFen


	// ====================================================================================================
	// public static String fenFromInformation
	//
	// Builds a fen string from a BoardInfo object
	//
	// Arguments--
	//
	//  boardInfo: the BoardInfo objec to turn into a string
	//
	// Returns--
	//
	//  A FEN string
	//
	public static String fenFromInformation(BoardInfo boardInfo) {
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

					int pieceType = piece.getType();
					String pieceChar = "";
					switch (pieceType) {
					case Piece.Type.PAWN:
						pieceChar = "P";
						break;
					case Piece.Type.KNIGHT:
						pieceChar = "N";
						break;
					case Piece.Type.BISHOP:
						pieceChar = "B";
						break;
					case Piece.Type.ROOK:
						pieceChar = "R";
						break;
					case Piece.Type.QUEEN:
						pieceChar = "Q";
						break;
					case Piece.Type.KING:
						pieceChar = "K";
						break;
					}

					if (piece.getColor() == Piece.Color.BLACK)
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
	// end: public static String fenFromInformation

}
// end: public class FenUtility
