// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// MoveUtility.java
// Chess
//
// Created by Jonathan Uhler on 4/15/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import java.util.*;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class MoveUtility
//
// Handles moves and move legality
//
public class MoveUtility {

    List<Short> moves; // List of move values for legal moves
    MoveData precomputedMoveData; // Basic move data/information

    int moveFlag = Move.Flag.none;


    // ====================================================================================================
    // public List<Move> generateMoves
    //
    // Generates legal moves
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // moves:   a list of legal moves
    //
    public List<Short> generateMoves() {
        moves = new ArrayList<>();
        precomputedMoveData = new MoveData();

//        //  White on bottom and white to move (pawns go up)  |  White on top and black to move (pawns go up)
//        if ((Board.whiteOnBottom && Board.colorToMove == 0) || (!Board.whiteOnBottom && Board.colorToMove == 1)) { pawnDirectionMultiplier = -1; }
//        //       White on bottom and black to move (pawns go down) | White on top and white to move (pawns go down)
//        else if ((Board.whiteOnBottom && Board.colorToMove == 1) || (!Board.whiteOnBottom && Board.colorToMove == 0)) { pawnDirectionMultiplier = 1; }

        for (int startTile = 0; startTile < 64; startTile++) {

            int piece = Chess.board.tile[startTile];

            if (piece != 0) { // Make sure there is an actual piece on the tile
                if (Piece.checkColor(piece, Chess.board.colorToMove, true)) { // Only check for peices of the color whose turn it is to move
                    if (Piece.checkSliding(piece)) { // Check sliding pieces
                        generateSlidingMoves(startTile, piece);
                    }
                    else if (Piece.pieceType(piece) == Piece.King) { // Check kings
                        generateKingMoves(startTile);
                    }
                    else if (Piece.pieceType(piece) == Piece.Knight) { // Check knights
                        generateKnightMoves(startTile);
                    }
                    else if (Piece.pieceType(piece) == Piece.Pawn) { // Check pawns
                        generatePawnMoves(startTile);
                    }
                }
            }

        }

        return moves;
    }
    // end: public List<Move> generateMoves


    // ====================================================================================================
    // void generateSlidingMoves
    //
    // Generates legal moves for sliding pieces (bishops, rooks, and queens)
    //
    // Arguments--
    //
    // startTile:   the starting tile for the piece being checked
    //
    // piece:       the sliding piece being checked
    //
    // Returns--
    //
    // None
    //
    void generateSlidingMoves(int startTile, int piece) {
        int startDirection = (Piece.pieceType(piece) == Piece.Bishop) ? 4 : 0;
        int endDirection = (Piece.pieceType(piece) == Piece.Rook) ? 4 : 8;

        for (int direction = startDirection; direction < endDirection; direction++) {
            for (int i = 0; i < precomputedMoveData.tilesToEdge[startTile][direction]; i++) {

                int endTile = startTile + precomputedMoveData.slidingOffsets[direction] * (i + 1); // Get the legal ending tile for the piece. Multiply by (i + 1) because sliding pieces can move an infinite distance in each of their directions
                int pieceOnEndTile = Chess.board.tile[endTile]; // Figure out if there is a piece on the ending tile

                // If the piece on the ending tile is of the same color, the move is illegal
                if (Piece.checkColor(pieceOnEndTile, Chess.board.friendlyColor, true)) { break; }

                // Add the move to the list of legal moves
                moves.add(new Move(startTile, endTile).moveValue);

                // If the piece on the ending tile is of the opposing color, you can capture, but not go past it
                if (Piece.checkColor(pieceOnEndTile, Chess.board.opponentColor, true)) { break; }

            }
        }
    }
    // end: void generateSlidingMoves


    // ====================================================================================================
    // void generateKingMoves
    //
    // Generates legal moves for kings
    //
    // Arguments--
    //
    // startTile:   the starting tile for the piece being checked
    //
    // Returns--
    //
    // None
    //
    void generateKingMoves(int startTile) {
        for (int direction = 0; direction < 8; direction++) {
            // Make sure there are available tiles in this direction
            if (precomputedMoveData.tilesToEdge[startTile][direction] == 0) { continue; }

            int endTile = startTile + precomputedMoveData.slidingOffsets[direction]; // Get the legal ending tile for the piece. Multiply by (i + 1) because sliding pieces can move an infinite distance in each of their directions
            int pieceOnEndTile = Chess.board.tile[endTile]; // Figure out if there is a piece on the ending tile

            // If the piece on the ending tile is of the same color, the move is illegal
            if (Piece.checkColor(pieceOnEndTile, Chess.board.friendlyColor, true)) { continue; }

            // Add the move to the list of legal moves
            moves.add(new Move(startTile, endTile).moveValue);
        }
    }
    // end: void generateKingMoves


    // ====================================================================================================
    // void generateKnightMoves
    //
    // Generates legal moves for knights
    //
    // Arguments--
    //
    // startTile:   the starting tile for the piece being checked
    //
    // Returns--
    //
    // None
    //
    void generateKnightMoves(int startTile) {
        List<Byte> knightOffsets = precomputedMoveData.knightOffsets.get(startTile);

        for (int endTile : knightOffsets) {
            int pieceOnEndTile = Chess.board.tile[endTile]; // Figure out if there is a piece on the ending tile

            // If the piece on the ending tile is of the same color, the move is illegal
            if (Piece.checkColor(pieceOnEndTile, Chess.board.friendlyColor, true)) { continue; }

            // Add the move to the list of legal moves
            moves.add(new Move(startTile, endTile).moveValue);
        }
    }
    // end: void generateKnightMoves


    // ====================================================================================================
    // void generatePawnMoves
    //
    // Generates legal moves for pawns
    //
    // Arguments--
    //
    // startTile:   the starting tile for the piece being checked
    //
    // Returns--
    //
    // None
    //
    int enPassantTile;
    ArrayList<Integer> pawnOffsets;
    //
    void generatePawnMoves(int startTile) {
        // Calculate en passant tile
        int enPassantCol = FenUtility.loadPositionFromFen(FenUtility.buildFenFromPosition()).enPassantCol;
        enPassantTile = (enPassantCol != -1) ? (2 * 8) + enPassantCol : -1; // Tile behind pawn that moved 2

        // Initialize pawn movement
        pawnOffsets = new ArrayList<>();
        pawnOffsets.add(-8);
        List<Integer> edgeTilesLeft = Arrays.asList(0, 8, 16, 24, 32, 40, 48, 56), edgeTilesRight = Arrays.asList(7, 15, 23, 31, 39, 47, 55, 63); // Check to make sure a pawn isn't capturing by wrapping around the board

        if (precomputedMoveData.pawnStartingTiles.contains(startTile)) { // Double pawn push
            pawnOffsets.add(-16);
        }

        for (int direction = 0; direction < pawnOffsets.size(); direction++) {
            int endTile = startTile + pawnOffsets.get(direction); // Get the legal ending tile for the piece. Multiply by (i + 1) because sliding pieces can move an infinite distance in each of their directions
            if (endTile < 0 || endTile > 63) { continue; }

            int pieceOnEndTile = Chess.board.tile[endTile]; // Figure out if there is a piece on the ending tile
            //                       Is the current direction being checked a movement of 8?   |  Is the capture on the board?               |  Is the direction going up
            int capturablePieceOne = (pawnOffsets.get(direction) == -8) ? (((endTile - 1) >= 0 && (endTile + 1) < 64) ? (Chess.board.tile[endTile - 1]) : 0) : 0;
            int capturablePieceTwo = (pawnOffsets.get(direction) == -8) ? (((endTile - 1) >= 0 && (endTile + 1) < 64) ? (Chess.board.tile[endTile + 1]) : 0) : 0;
            int enPassantPiece = (enPassantTile != -1) ? Chess.board.tile[enPassantTile + 8] : 0;

            // If there is a piece on the ending tile, the pawn cannot move there (friendly or not)
            if (pieceOnEndTile != 0) {
                pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-16))); // Remove the option to move two squares and jump over the piece in front
                pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-8))); // Remove the option to move one square forward

                if (!(Piece.checkColor(capturablePieceOne, Chess.board.opponentColor, true)) && !(Piece.checkColor(capturablePieceTwo, Chess.board.opponentColor, true))) { continue; } // Make sure pawns don't capture the piece directly in front of them
            }

            if (enPassantPiece != 0) {
                if (enPassantTile == (startTile - 9) && !edgeTilesRight.contains((startTile - 9))) {
                    pawnOffsets.add(-9);
                }
                if (enPassantTile == (startTile - 7) && !edgeTilesLeft.contains((startTile - 7))) {
                    pawnOffsets.add(-7);
                }

                generateEnPassantCaptures(startTile);
            }

            // Regular captures
            if (Piece.checkColor(capturablePieceOne, Chess.board.opponentColor, true) || Piece.checkColor(capturablePieceTwo, Chess.board.opponentColor, true)) {
                if (Piece.checkColor(capturablePieceOne, Chess.board.opponentColor, true) && !edgeTilesLeft.contains(endTile)) {
                    pawnOffsets.add(-9);
                }
                if (Piece.checkColor(capturablePieceTwo, Chess.board.opponentColor, true) && !edgeTilesRight.contains(endTile)) {
                    pawnOffsets.add(-7);
                }

                generatePawnCaptures(startTile);
                continue;
            }

            // Pawn promoted
            moveFlag = Move.Flag.none;
            if (MoveData.pawnPromotionTiles.contains(endTile)) {
                moveFlag = Move.Flag.promoteToQueen; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToRook; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToKnight; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToBishop; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                continue;
            }

            // Pawn moved two forward
            if (pawnOffsets.get(direction) == -16) {
                moveFlag = Move.Flag.pawnTwoForward;
            }

            // Add the move to the list of legal moves
            moves.add(new Move(startTile, endTile, moveFlag).moveValue);
        }
    }
    // end: void generatePawnMoves


    // ====================================================================================================
    // void generatePawnCaptures
    //
    // Generates legal captures for pawns
    //
    // Arguments--
    //
    // startTile:   the starting tile for the piece being checked
    //
    // Returns--
    //
    // None
    //
    void generatePawnCaptures(int startTile) {
        for (int pawnOffset : pawnOffsets) {
            int endTile = startTile + pawnOffset; // Get the legal ending tile for the piece. Multiply by (i + 1) because sliding pieces can move an infinite distance in each of their directions
            if (endTile < 0 || endTile > 63) { continue; }

            moveFlag = Move.Flag.none;
            // The pawn can promote after capturing
            if (MoveData.pawnPromotionTiles.contains(endTile)) {
                moveFlag = Move.Flag.promoteToQueen; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToRook; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToKnight; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToBishop; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                continue;
            }

            // Add the move to the list of legal moves
            moves.add(new Move(startTile, endTile, moveFlag).moveValue);
        }

        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-9)));
        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-9)));
    }
    // end: void generatePawnCaptures


    // ====================================================================================================
    // void generateEnPassantCaptures
    //
    // Generates legal en passant captures for pawns
    //
    // Arguments--
    //
    // startTile:   the starting tile for the piece being checked
    //
    // Returns--
    //
    // None
    //
    void generateEnPassantCaptures(int startTile) {
        for (int pawnOffset : pawnOffsets) {
            int endTile = startTile + pawnOffset; // Find the end tile
            int enPassantPiece = (enPassantTile != -1) ? Chess.board.tile[enPassantTile + 8] : 0; // Find the piece being targeted by the en passant move

            moveFlag = Move.Flag.none;
            // End tile is the en passant tile and the piece is another pawn
            if (endTile == enPassantTile && Piece.pieceType(enPassantPiece) == Piece.Pawn) {
                moveFlag = Move.Flag.enPassantCapture; // Mark with EP flag
            }

            moves.add(new Move(startTile, endTile, moveFlag).moveValue); // Save the move
        }

        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-9)));
        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-7)));
    }
    // end: void generateEnPassantCaptures

}
