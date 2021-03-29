// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// FenUtility.java
// Chess
//
// Created by Jonathan Uhler on 3/25/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import java.util.*;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class FenInfo
//
// Information about the loaded FEN string
//
class FenInfo {
    public static int[] tiles; // Internal memory for every tile that makes up the chessboard
    public boolean whiteCastleKingside;
    public boolean whiteCastleQueenside;
    public boolean blackCastleKingside;
    public boolean blackCastleQueenside;
    public int enPassantRow; // Which row is open for en passant
    public int plies; // Number of plies this game
    public boolean whiteToMove; // Is it white's turn. If not, then it must be black's turn

    // FenInfo constructor. Sets the number of tiles to 64
    public FenInfo() {
        tiles = new int[64];
    }
}
// end: public class FenInfo


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class FenUtility
//
// Manages FEN strings
//
public class FenUtility {

    // FEN Standard Notation-- https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
    //
    // p: pawn, n: knight, b: bishop, r: rook, k: king, q: queen  \
    // lower case letters: black pieces                            |
    // upper case letters: white pieces                            |-- when splitting a FEN string by spaces, this is in index 0
    // numbers: empty spaces in a given row                        |
    // "/": move to the next row down                             /
    // w: white/black to move next (white is w like in startFen, b is black) -- when splitting a FEN string by spaces, this is in index 1
    // KQkq: determines if castling is available (letter case determines color and the letter determines which side)        \ -- when splitting a FEN string by spaces, this is in index 2
    // Note for castling: this only shows if the rooks have moved, and not if there are pieces that would prevent a castle  /
    // -: determines en passant possibilities (- for none, tile coord (eg "e5") for possibilities) -- when splitting a FEN string by spaces, this is in index 3
    // 0: halfmove clock, this is the number of halfmoves (1ply moves) since the last capture or pawn advance -- when splitting a FEN string by spaces, this is in index 4
    // 1: fullmove clock, this is the number of full moves. It starts at 1 and is incremented after black's move -- when splitting a FEN string by spaces, this is in index 5

    // Starting position of any chess game
    public static final String startFen = "RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr w KQkq - 0 1"; // Player color black: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

    // Add the corresponding characters for each piece type (ignore letter case for now, assume no piece color
    static Hashtable<Character, Integer> pieceTypeFromFen = new Hashtable<Character, Integer>() {{
        put('k', Piece.King);
        put('p', Piece.Pawn);
        put('n', Piece.Knight);
        put('b', Piece.Bishop);
        put('r', Piece.Rook);
        put('q', Piece.Queen);
    }};


    // ====================================================================================================
    // public loadPositionFromFen
    //
    // Loads a given board position from a FEN string
    //
    // Arguments--
    //
    // fen:     the fen string to load
    //
    // Returns--
    //
    // None
    //
    public static FenInfo loadPositionFromFen(String fen) {
        FenInfo fenInfo = new FenInfo(); // Create a new instance of the FenInfo class
        String[] fenSplit = fen.split(" "); // Split the fen string given by spaces

        int row = 7; // Start on the bottom row
        int col = 0; // Start in the left-most column

        // Loop through each character of the board setup. Set the board setup info to a char array in order for the foreach x in y to work
        for (char fenChar : fenSplit[0].toCharArray()) {
            if (fenChar == '/') {
                row--; // Decrease the current row towards the top of the board
                col = 0; // Reset the column
            }
            else {
                if (Character.isDigit(fenChar)) {
                    col += Character.getNumericValue(fenChar); // If the current character is a number, increase the column by that amount
                }
                else {
                    // If fenChar is uppercase, the piece is white, else it is black
                    int color = (Character.isUpperCase(fenChar)) ? Piece.White : Piece.Black;
                    // Find the piece type corresponding to the fenChar from the hashtable above
                    int type = pieceTypeFromFen.get(Character.toLowerCase(fenChar));

                    // Save the piece color and type
                    fenInfo.tiles[row * 8 + col] = type | color; // Use bitwise OR to create the binary value for the piece
                    col++;
                }
            }
        }

        fenInfo.whiteToMove = (fenSplit[1].equals('w')); // Set the boolean value whiteToMove depending on the FEN string
        String rightToCastle = (fenSplit.length > 2) ? fenSplit[2] : "KQkq"; // If the fenSplit array has 3+ elements, then it is safe to use index [2], else just use the castling string "KQkq"
        // Determine the legality of castling
        fenInfo.whiteCastleKingside = rightToCastle.contains("K");
        fenInfo.whiteCastleQueenside = rightToCastle.contains("Q");
        fenInfo.blackCastleKingside = rightToCastle.contains("k");
        fenInfo.blackCastleQueenside = rightToCastle.contains("q");

        // MARK: en passant row and halfmove clock both need to be implemented here

        // Return the fenInfo object
        return fenInfo;
    }
    // public loadPositionFromFen

}
// end: public class FenUtilty