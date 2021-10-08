// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// FenUtility.java
// Chess
//
// Created by Jonathan Uhler on 3/25/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package util;


import main.Chess;
import piece.Piece;

import java.util.*;


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
    private static HashMap<Character, Integer> pieceTypeFromFen = new HashMap<Character, Integer>() {{
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

        int row = 0; // Start on the bottom row
        int col = 0; // Start in the left-most column

        // Loop through each character of the board setup. Set the board setup info to a char array in order for the foreach x in y to work
        for (char fenChar : fenSplit[0].toCharArray()) {
            if (fenChar == '/') {
                row++; // Decrease the current row towards the top of the board
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

        // En passant col
        if (fenSplit[3].equals("-")) {
            fenInfo.enPassantCol = -1;
        }
        else {
            HashMap<Character, Integer> colNames = new HashMap<>();
            colNames.put('a', 0);
            colNames.put('b', 1);
            colNames.put('c', 2);
            colNames.put('d', 3);
            colNames.put('e', 4);
            colNames.put('f', 5);
            colNames.put('g', 6);
            colNames.put('h', 7);

            fenInfo.enPassantCol = colNames.get(fenSplit[3].charAt(0));
        }
        // En passant tile
        fenInfo.enPassantTile = fenSplit[3];

        // Number of halfmoves
        fenInfo.halfmoves = Integer.parseInt(fenSplit[4]);

        // Number of fullmoves (for 50 move rule)
        fenInfo.fullmoves = Integer.parseInt(fenSplit[5]);

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
                int piece = Chess.getBoard().getTile()[tile]; // Find the piece (if any) on the current tile

                if (piece != 0) { // Make sure a piece exists on the current tile
                    if (numEmptyCols != 0) {
                        fen.append(numEmptyCols); // If there were some empty tiles, add that to the fen string,
                        numEmptyCols = 0; // Then reset the number of empty tiles
                    }

                    boolean whitePiece = Piece.checkColor(piece, Piece.White, false); // Figure out the color of the piece
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
        fen.append((Chess.getBoard().getWhitesMove()) ? 'w' : 'b'); // Add whose turn it is to move

        // Castling legality
        boolean whiteKingside = Chess.getBoard().getCastleK();
        boolean whiteQueenside = Chess.getBoard().getCastleQ();
        boolean blackKingside = Chess.getBoard().getCastlek();
        boolean blackQueenside = Chess.getBoard().getCastleq();
        // Add castling characters
        fen.append(' ');
        fen.append((whiteKingside) ? "K" : "");
        fen.append((whiteQueenside) ? "Q" : "");
        fen.append((blackKingside) ? "k" : "");
        fen.append((blackQueenside) ? "q" : "");
        fen.append((!whiteKingside && !whiteQueenside && !blackKingside && !blackQueenside) ? "-" : ""); // No legal castles can be made

        // En passant legality
        fen.append(' '); // Add a space
        String enPassantTile = Chess.getBoard().getEnPassantTile(); // Get the en passant col

        if (enPassantTile.equals("-")) {
            fen.append('-');
        }
        else {
            fen.append(enPassantTile);
        }

        // Half move clock
        fen.append(' '); // Add a space
        fen.append(Chess.getBoard().getFiftyMoveRule());

        // Full move clock (for 50 move rule)
        fen.append(' '); // Add a space
        fen.append(Chess.getBoard().getFullmoves()); // Figure out the number of full moves

        // Return the position
        return fen.toString();
    }
    // end: public static String buildFenFromPosition


    // ====================================================================================================
    // public static String changePlayerPerspective
    //
    // Properly reverses a fen string to show the perspective of the game from the other player
    //
    // Arguments--
    //
    // fen:             the fen to reverse
    //
    // Returns--
    //
    // fenReversed:     the reversed fen
    //
    public static String changePlayerPerspective(String fen) {
        StringBuilder fenReversed = new StringBuilder(); // Create an empty string builder to add to

        String[] fenSplit = fen.split(" "); // Split the fen string into its components
        String fenDetails = " " + fenSplit[1] + " " + fenSplit[2] + " " + fenSplit[3] + " " + fenSplit[4] + " " + fenSplit[5]; // Recompile the details of the fen string without the piece position

        String[] positionSplit = fenSplit[0].split("/"); // Split the piece position by the / delimiter
        List<String> reversedPositionsSplit = new ArrayList<>(); // Create an empty list to hold the parts of the piece position

        for (String positionSection : positionSplit) { // For each section of the piece position
            StringBuilder temp = new StringBuilder(positionSection); // Reverse that section
            temp.reverse();
            reversedPositionsSplit.add(temp.toString()); // Add the reversed section to a list of sections
        }

        Collections.reverse(reversedPositionsSplit); // Reverse the list of reversed sections to finally change perspective
        String positionReversed = String.join("/", reversedPositionsSplit); // Rebuild the piece position

        fenReversed.append(positionReversed); // Rebuild the fen string
        fenReversed.append(fenDetails);

        return fenReversed.toString(); // Return the reversed string
    }
    // end: public static String changePlayerPerspective

}
// end: public class FenUtilty