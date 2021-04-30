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

    MoveData precomputedMoveData; // Basic move data/information
    Board boardToUse;
    int moveFlag = Move.Flag.none;


    // ====================================================================================================
    // List<Move> generateMoves
    //
    // Generates legal moves
    //
    // Arguments--
    //
    // board:   the board to generate moves for
    //
    // Returns--
    //
    // pseudoLegalMoves:    a list of legal moves
    //
    List<Move> generateMoves(Board board) {
        List<Move> pseudoLegalMoves = new ArrayList<>();
        precomputedMoveData = new MoveData();
        boardToUse = board;

        for (int startTile = 0; startTile < 64; startTile++) {
            int piece = boardToUse.tile[startTile];

            if (piece != 0) { // Make sure there is an actual piece on the tile
                if (Piece.checkColor(piece, boardToUse.colorToMove, true)) { // Only check for peices of the color whose turn it is to move
                    if (Piece.checkSliding(piece)) { // Check sliding pieces
                        pseudoLegalMoves.addAll(generateSlidingMoves(startTile, piece));
                    }
                    else if (Piece.pieceType(piece) == Piece.King) { // Check kings
                        pseudoLegalMoves.addAll(generateKingMoves(startTile));
                    }
                    else if (Piece.pieceType(piece) == Piece.Knight) { // Check knights
                        pseudoLegalMoves.addAll(generateKnightMoves(startTile));
                    }
                    else if (Piece.pieceType(piece) == Piece.Pawn) { // Check pawns
                        pseudoLegalMoves.addAll(generatePawnMoves(startTile));
                    }
                }
            }
        }

        return pseudoLegalMoves;
    }
    // end: List<Move> generateMoves


    // ====================================================================================================
    // List<Move> generateSlidingMoves
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
    // movesGenerated:  a list of sliding moves generated
    //
    List<Move> generateSlidingMoves(int startTile, int piece) {
        List<Move> movesGenerated = new ArrayList<>();

        int startDirection = (Piece.pieceType(piece) == Piece.Bishop) ? 4 : 0;
        int endDirection = (Piece.pieceType(piece) == Piece.Rook) ? 4 : 8;

        for (int direction = startDirection; direction < endDirection; direction++) {
            for (int i = 0; i < precomputedMoveData.tilesToEdge[startTile][direction]; i++) {

                int endTile = startTile + precomputedMoveData.slidingOffsets[direction] * (i + 1); // Get the legal ending tile for the piece. Multiply by (i + 1) because sliding pieces can move an infinite distance in each of their directions
                int pieceOnEndTile = boardToUse.tile[endTile]; // Figure out if there is a piece on the ending tile

                // If the piece on the ending tile is of the same color, the move is illegal
                if (Piece.checkColor(pieceOnEndTile, boardToUse.friendlyColor, true)) { break; }

                // Add the move to the list of legal moves
                movesGenerated.add(new Move(startTile, endTile));

                // If the piece on the ending tile is of the opposing color, you can capture, but not go past it
                if (Piece.checkColor(pieceOnEndTile, boardToUse.opponentColor, true)) { break; }

            }
        }

        return movesGenerated;
    }
    // end: List<Move> generateSlidingMoves


    // ====================================================================================================
    // List<Move> generateKingMoves
    //
    // Generates legal moves for kings
    //
    // Arguments--
    //
    // startTile:   the starting tile for the piece being checked
    //
    // Returns--
    //
    // movesGenerated:  a list of king moves generated
    //
    List<Move> generateKingMoves(int startTile) {
        List<Move> movesGenerated = new ArrayList<>();

        for (int direction = 0; direction < 8; direction++) {
            // Make sure there are available tiles in this direction
            if (precomputedMoveData.tilesToEdge[startTile][direction] == 0) { continue; }

            int endTile = startTile + precomputedMoveData.slidingOffsets[direction]; // Get the legal ending tile for the piece. Multiply by (i + 1) because sliding pieces can move an infinite distance in each of their directions
            int pieceOnEndTile = boardToUse.tile[endTile]; // Figure out if there is a piece on the ending tile

            // If the piece on the ending tile is of the same color, the move is illegal
            if (Piece.checkColor(pieceOnEndTile, boardToUse.friendlyColor, true)) { continue; }

            // Add the move to the list of legal moves
            movesGenerated.add(new Move(startTile, endTile));
        }

        return movesGenerated;
    }
    // end: List<Move> generateKingMoves


    // ====================================================================================================
    // List<Move> generateKnightMoves
    //
    // Generates legal moves for knights
    //
    // Arguments--
    //
    // startTile:   the starting tile for the piece being checked
    //
    // Returns--
    //
    // movesGenerated:  a list of knight moves generated
    //
    List<Move> generateKnightMoves(int startTile) {
        List<Move> movesGenerated = new ArrayList<>();
        List<Byte> knightOffsets = precomputedMoveData.knightOffsets.get(startTile);

        for (int endTile : knightOffsets) {
            int pieceOnEndTile = boardToUse.tile[endTile]; // Figure out if there is a piece on the ending tile

            // If the piece on the ending tile is of the same color, the move is illegal
            if (Piece.checkColor(pieceOnEndTile, boardToUse.friendlyColor, true)) { continue; }

            // Add the move to the list of legal moves
            movesGenerated.add(new Move(startTile, endTile));
        }

        return movesGenerated;
    }
    // end: List<Move> generateKnightMoves


    // ====================================================================================================
    // List<Move> generatePawnMoves
    //
    // Generates legal moves for pawns
    //
    // Arguments--
    //
    // startTile:   the starting tile for the piece being checked
    //
    // Returns--
    //
    // movesGenerated:  a list of pawn moves generated
    //
    int enPassantTile;
    ArrayList<Integer> pawnOffsets;
    //
    List<Move> generatePawnMoves(int startTile) {
        List<Move> movesGenerated = new ArrayList<>();

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

            int pieceOnEndTile = boardToUse.tile[endTile]; // Figure out if there is a piece on the ending tile
            //                       Is the current direction being checked a movement of 8?   |  Is the capture on the board?               |  Is the direction going up
            int capturablePieceOne = (pawnOffsets.get(direction) == -8) ? (((endTile - 1) >= 0 && (endTile + 1) < 64) ? (boardToUse.tile[endTile - 1]) : 0) : 0;
            int capturablePieceTwo = (pawnOffsets.get(direction) == -8) ? (((endTile - 1) >= 0 && (endTile + 1) < 64) ? (boardToUse.tile[endTile + 1]) : 0) : 0;
            int enPassantPiece = (enPassantTile != -1) ? boardToUse.tile[enPassantTile + 8] : 0;

            // If there is a piece on the ending tile, the pawn cannot move there (friendly or not)
            if (pieceOnEndTile != 0) {
                pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-16))); // Remove the option to move two squares and jump over the piece in front
                pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-8))); // Remove the option to move one square forward

                if (!(Piece.checkColor(capturablePieceOne, boardToUse.opponentColor, true)) && !(Piece.checkColor(capturablePieceTwo, boardToUse.opponentColor, true))) { continue; } // Make sure pawns don't capture the piece directly in front of them
            }

            // En passant captures
            if (enPassantPiece != 0) {
                if (enPassantTile == (startTile - 9) && !edgeTilesRight.contains((startTile - 9))) {
                    pawnOffsets.add(-9);
                }
                if (enPassantTile == (startTile - 7) && !edgeTilesLeft.contains((startTile - 7))) {
                    pawnOffsets.add(-7);
                }

                movesGenerated.addAll(generateEnPassantCaptures(startTile));
            }

            // Regular captures
            if (Piece.checkColor(capturablePieceOne, boardToUse.opponentColor, true) || Piece.checkColor(capturablePieceTwo, boardToUse.opponentColor, true)) {
                if (Piece.checkColor(capturablePieceOne, boardToUse.opponentColor, true) && !edgeTilesLeft.contains(endTile)) {
                    pawnOffsets.add(-9);
                }
                if (Piece.checkColor(capturablePieceTwo, boardToUse.opponentColor, true) && !edgeTilesRight.contains(endTile)) {
                    pawnOffsets.add(-7);
                }

                movesGenerated.addAll(generatePawnCaptures(startTile));
                continue;
            }

            // Pawn promoted
            moveFlag = Move.Flag.none;
            if (MoveData.pawnPromotionTiles.contains(endTile)) {
                moveFlag = Move.Flag.promoteToQueen; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                moveFlag = Move.Flag.promoteToRook; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                moveFlag = Move.Flag.promoteToKnight; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                moveFlag = Move.Flag.promoteToBishop; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                continue;
            }

            // Pawn moved two forward
            if (pawnOffsets.get(direction) == -16) {
                moveFlag = Move.Flag.pawnTwoForward;
            }

            // Add the move to the list of legal moves
            movesGenerated.add(new Move(startTile, endTile, moveFlag));
        }

        return movesGenerated;
    }
    // end: List<Move> generatePawnMoves


    // ====================================================================================================
    // List<Move> generatePawnCaptures
    //
    // Generates legal captures for pawns
    //
    // Arguments--
    //
    // startTile:   the starting tile for the piece being checked
    //
    // Returns--
    //
    // movesGenerated:  a list of pawn captures generated
    //
    List<Move> generatePawnCaptures(int startTile) {
        List<Move> movesGenerated = new ArrayList<>();

        for (int pawnOffset : pawnOffsets) {
            int endTile = startTile + pawnOffset; // Get the legal ending tile for the piece. Multiply by (i + 1) because sliding pieces can move an infinite distance in each of their directions
            if (endTile < 0 || endTile > 63) { continue; }

            moveFlag = Move.Flag.none;
            // The pawn can promote after capturing
            if (MoveData.pawnPromotionTiles.contains(endTile)) {
                moveFlag = Move.Flag.promoteToQueen; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                moveFlag = Move.Flag.promoteToRook; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                moveFlag = Move.Flag.promoteToKnight; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                moveFlag = Move.Flag.promoteToBishop; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                continue;
            }

            // Add the move to the list of legal moves
            movesGenerated.add(new Move(startTile, endTile, moveFlag));
        }

        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-9)));
        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-9)));

        return movesGenerated;
    }
    // end: List<Move> generatePawnCaptures


    // ====================================================================================================
    // List<Move> generateEnPassantCaptures
    //
    // Generates legal en passant captures for pawns
    //
    // Arguments--
    //
    // startTile:   the starting tile for the piece being checked
    //
    // Returns--
    //
    // movesGenerated:  a list of en passant captures generated
    //
    List<Move> generateEnPassantCaptures(int startTile) {
        List<Move> movesGenerated = new ArrayList<>();

        for (int pawnOffset : pawnOffsets) {
            int endTile = startTile + pawnOffset; // Find the end tile
            int enPassantPiece = (enPassantTile != -1) ? boardToUse.tile[enPassantTile + 8] : 0; // Find the piece being targeted by the en passant move

            moveFlag = Move.Flag.none;
            // End tile is the en passant tile and the piece is another pawn
            if (endTile == enPassantTile && Piece.pieceType(enPassantPiece) == Piece.Pawn) {
                moveFlag = Move.Flag.enPassantCapture; // Mark with EP flag
            }

            movesGenerated.add(new Move(startTile, endTile, moveFlag)); // Save the move
        }

        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-9)));
        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(-7)));

        return movesGenerated;
    }
    // end: List<Move> generateEnPassantCaptures

}
