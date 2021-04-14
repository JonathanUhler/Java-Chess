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
    public int[] tiles; // Internal memory for every tile that makes up the chessboard
    public boolean whiteCastleKingside;
    public boolean whiteCastleQueenside;
    public boolean blackCastleKingside;
    public boolean blackCastleQueenside;
    public int enPassantRow; // Which row is open for en passant
    public int plies; // Number of plies this game
    public boolean whiteToMove; // Is it white's turn. If not, then it must be black's turn


    // ----------------------------------------------------------------------------------------------------
    // public FenInfo
    //
    // Sets the number of tiles to 64
    //
    public FenInfo() {
        tiles = new int[64];
    }
    // end: public FenInfo
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
    // public static FenInfo loadPositionFromFen
    //
    // Loads a given board position from a FEN string
    //
    // Arguments--
    //
    // fen:     the fen string to load
    //
    // Returns--
    //
    // fenInfo: the information about the current fen position
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

        fenInfo.whiteToMove = (fenSplit[1].equals("w")); // Set the boolean value whiteToMove depending on the FEN string
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
    // public static FenInfo loadPositionFromFen


    // ====================================================================================================
    // public static String buildFenFromPosition
    //
    // Takes information about the current board position and returns the position's FEN string
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // fen:     the fen string for the current position
    //
    public static String buildFenFromPosition() {
        StringBuilder fen = new StringBuilder(); // Fen string to be built

        for (int row = 0; row < 8; row++) {

            int numEmptyCols = 0; // Number of black spaces

            for (int col = 0; col < 8; col++) {

                int tile = row * 8 + col; // Store the current tile being looked at
                int piece = Board.tile[tile]; // Find the piece (if any) on the current tile

                if (piece != 0) { // Make sure a piece exists on the current tile
                    if (numEmptyCols != 0) {
                        fen.append(numEmptyCols); // If there were some empty tiles, add that to the fen string,
                        numEmptyCols = 0; // Then reset the number of empty tiles
                    }

                    boolean whitePiece = Piece.findColor(piece, Piece.White); // Figure out the color of the piece
                    int pieceType = Piece.pieceType(piece); // Figure out what type of piece it is
                    char pieceChar = ' ';

                    switch (pieceType) { // Set the corresponding letter to the piece type
                        case Piece.Rook:
                            pieceChar = 'R';
                            break;
                        case Piece.Knight:
                            pieceChar = 'N';
                            break;
                        case Piece.Bishop:
                            pieceChar = 'B';
                            break;
                        case Piece.Queen:
                            pieceChar = 'Q';
                            break;
                        case Piece.King:
                            pieceChar = 'K';
                            break;
                        case Piece.Pawn:
                            pieceChar = 'P';
                            break;
                    }

                    //         If the piece is white, keep it uppercase | If the piece is black, set it to lowercase
                    fen.append((whitePiece) ? Character.toString(pieceChar) : Character.toString(Character.toLowerCase(pieceChar)));
                }
                else {
                    numEmptyCols++; // If there was no piece, increase the number of empty tiles
                }

            }

            if (numEmptyCols != 0) {
                fen.append(numEmptyCols);
            }
            if (row != 7) {
                fen.append('/'); // If a new row is started, add the delimiter for a row
            }

        }

        // Player to move
        fen.append(' '); // Add a space
        fen.append((Board.whitesMove) ? 'w' : 'b'); // Add whose turn it is to move

        // Castling legality
        boolean whiteKingside = (Board.currentGameState & 1) == 1;
        boolean whiteQueenside = (Board.currentGameState >> 1 & 1) == 1;
        boolean blackKingside = (Board.currentGameState >> 2 & 1) == 1;
        boolean blackQueenside = (Board.currentGameState >> 3 & 1) == 1;
        fen.append(' ');
        fen.append((whiteKingside) ? "K" : "");
        fen.append((whiteQueenside) ? "Q" : "");
        fen.append((blackKingside) ? "k" : "");
        fen.append((blackQueenside) ? "q" : "");
        fen.append(((Board.currentGameState & 15) == 0) ? "-" : "");

        // En passant legality
        fen.append(' '); // Add a space
        int enPassantRow = (Board.currentGameState >> 4) & 15; // Get the en passant row
        String[] colNames = {"a", "b", "c", "d", "e", "f", "g", "h"};

        if (enPassantRow == 0) { // If the row = 0, no en passant is available
            fen.append('-');
        }
        else { // If the row doesn't = 0, get the en passant coordinates
            String fileName = colNames[enPassantRow - 1].toString();
            int enPassantCol = (Board.whitesMove) ? 6 : 3;
            fen.append(fileName).append(enPassantCol);
        }

        // Half move clock (for 50 move rule)
        fen.append(' '); // Add a space
        fen.append(Board.fiftyMoveRule);

        // Full move clock
        fen.append(' '); // Add a space
        fen.append((Board.plies / 2) + 1); // Figure out the number of full moves

        // Return the position
        return fen.toString();
    }
    // end: public static String buildFenFromPosition

}
// end: public class FenUtilty