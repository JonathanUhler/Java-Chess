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

            int piece = Board.tile[startTile];

            if (piece != 0) { // Make sure there is an actual piece on the tile
                if (Piece.checkColor(piece, Board.colorToMove, true)) { // Only check for peices of the color whose turn it is to move
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
                int pieceOnEndTile = Board.tile[endTile]; // Figure out if there is a piece on the ending tile

                // If the piece on the ending tile is of the same color, the move is illegal
                if (Piece.checkColor(pieceOnEndTile, Board.friendlyColor, true)) { break; }

                // Add the move to the list of legal moves
                moves.add(new Move(startTile, endTile).moveValue);

                // If the piece on the ending tile is of the opposing color, you can capture, but not go past it
                if (Piece.checkColor(pieceOnEndTile, Board.opponentColor, true)) { break; }

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
            int pieceOnEndTile = Board.tile[endTile]; // Figure out if there is a piece on the ending tile

            // If the piece on the ending tile is of the same color, the move is illegal
            if (Piece.checkColor(pieceOnEndTile, Board.friendlyColor, true)) { continue; }

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
            int pieceOnEndTile = Board.tile[endTile]; // Figure out if there is a piece on the ending tile

            // If the piece on the ending tile is of the same color, the move is illegal
            if (Piece.checkColor(pieceOnEndTile, Board.friendlyColor, true)) { continue; }

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
    int pawnDirectionMultiplier = -1;
    ArrayList<Integer> pawnOffsets;
    //
    void generatePawnMoves(int startTile) {
        pawnOffsets = new ArrayList<>();
        pawnOffsets.add(8 * pawnDirectionMultiplier);
        List<Integer> edgeTilesLeft = Arrays.asList(0, 8, 16, 24, 32, 40, 48, 56), edgeTilesRight = Arrays.asList(7, 15, 23, 31, 39, 47, 55, 63); // Check to make sure a pawn isn't capturing by wrapping around the board

        if (precomputedMoveData.pawnStartingTiles.contains(startTile)) { // Double pawn push
            pawnOffsets.add(16  * pawnDirectionMultiplier);
        }

        for (int direction = 0; direction < pawnOffsets.size(); direction++) {
            int endTile = startTile + pawnOffsets.get(direction); // Get the legal ending tile for the piece. Multiply by (i + 1) because sliding pieces can move an infinite distance in each of their directions
            if (endTile < 0 || endTile > 63) { continue; }

            int pieceOnEndTile = Board.tile[endTile]; // Figure out if there is a piece on the ending tile
            int capturablePieceOne = (pawnOffsets.get(direction) == (8 * pawnDirectionMultiplier)) ? (((endTile - 1) >= 0 && (endTile + 1) < 64) ? ((pawnDirectionMultiplier == -1) ? Board.tile[endTile - 1] : Board.tile[endTile + 1]) : 0) : 0;
            int capturablePieceTwo = (pawnOffsets.get(direction) == (8 * pawnDirectionMultiplier)) ? (((endTile - 1) >= 0 && (endTile + 1) < 64) ? ((pawnDirectionMultiplier == -1) ? Board.tile[endTile + 1] : Board.tile[endTile - 1]) : 0) : 0;

            // If there is a piece on the ending tile, the pawn cannot move there (friendly or not)
            if (pieceOnEndTile != 0) {
                pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(16 * pawnDirectionMultiplier))); // Remove the option to move two squares and jump over the piece in front
                pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(8 * pawnDirectionMultiplier))); // Remove the option to move one square forward

                if (!(Piece.checkColor(capturablePieceOne, Board.opponentColor, true)) && !(Piece.checkColor(capturablePieceTwo, Board.opponentColor, true))) { continue; } // Make sure pawns don't capture the piece directly in front of them
            }

            // If there is an enemy piece on an adjacent tile, it can be captured
            if (Piece.checkColor(capturablePieceOne, Board.opponentColor, true) || Piece.checkColor(capturablePieceTwo, Board.opponentColor, true)) {
                if (Piece.checkColor(capturablePieceOne, Board.opponentColor, true) && !edgeTilesLeft.contains(endTile)) {
                    pawnOffsets.add(9 * pawnDirectionMultiplier);
                }
                if (Piece.checkColor(capturablePieceTwo, Board.opponentColor, true) && !edgeTilesRight.contains(endTile)) {
                    pawnOffsets.add(7 * pawnDirectionMultiplier);
                }

                generatePawnCaptures(startTile);
                continue;
            }

            moveFlag = Move.Flag.none;
            if (MoveData.pawnPromotionTiles.contains(endTile)) {
                moveFlag = Move.Flag.promoteToQueen; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToRook; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToKnight; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToBishop; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                break;
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

            if (MoveData.pawnPromotionTiles.contains(endTile)) {
                moveFlag = Move.Flag.promoteToQueen; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToRook; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToKnight; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                moveFlag = Move.Flag.promoteToBishop; moves.add(new Move(startTile, endTile, moveFlag).moveValue);
                break;
            }

            // Add the move to the list of legal moves
            moves.add(new Move(startTile, endTile, moveFlag).moveValue);
        }

        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(9 * pawnDirectionMultiplier)));
        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(7 * pawnDirectionMultiplier)));
    }
    // end: void generatePawnCaptures

}
