// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// MoveUtility.java
// Chess
//
// Created by Jonathan Uhler on 4/15/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package util;


import board.Board;
import main.Chess;
import move.Move;
import move.MoveData;
import piece.Piece;

import java.util.*;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class MoveUtility
//
// Handles move generation
//
public class MoveUtility {

    MoveData precomputedMoveData; // Basic move data/information
    Board boardToUse;
    int moveFlag = Move.Flag.none;


    // ====================================================================================================
    // public static List<Integer> returnMoveValues
    //
    // Returns a list of move values from a list of move objects
    //
    // Arguments--
    //
    // listOfMoves: the list of move objects
    //
    // Returns--
    //
    // moveVals:    the list of move values
    //
    public static List<Integer> returnMoveValues(List<Move> listOfMoves) {
        List<Integer> moveVals = new ArrayList<>();

        for (Move move : listOfMoves) {
            moveVals.add(move.getMoveValue());
        }

        return moveVals;
    }
    // end: public static List<Integer> returnMoveValues


    // ====================================================================================================
    // public static List<Integer> returnStartingTiles
    //
    // Returns a list of starting tiles from a list of move objects
    //
    // Arguments--
    //
    // listOfMoves: the list of move objects
    //
    // Returns--
    //
    // startTiles:    the list of start tiles
    //
    public static List<Integer> returnStartingTiles(List<Move> listOfMoves) {
        List<Integer> startTiles = new ArrayList<>();

        for (Move move : listOfMoves) {
            startTiles.add(move.startTile());
        }

        return startTiles;
    }
    // end: public static List<Integer> returnStartingTiles


    // ====================================================================================================
    // public static List<Integer> returnEndingTiles
    //
    // Returns a list of ending tiles from a list of move objects
    //
    // Arguments--
    //
    // listOfMoves: the list of move objects
    //
    // Returns--
    //
    // endTiles:    the list of end tiles
    //
    public static List<Integer> returnEndingTiles(List<Move> listOfMoves) {
        List<Integer> endTiles = new ArrayList<>();

        for (Move move : listOfMoves) {
            endTiles.add(move.endTile());
        }

        return endTiles;
    }
    // end: public static List<Integer> returnEndingTiles


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
    public List<Move> generateMoves(Board board) {
        List<Move> pseudoLegalMoves = new ArrayList<>();
        precomputedMoveData = new MoveData();
        boardToUse = board;

        for (int startTile = 0; startTile < 64; startTile++) {
            int piece = boardToUse.getTile()[startTile];

            if (piece != 0) { // Make sure there is an actual piece on the tile
                if (Piece.checkColor(piece, boardToUse.getColorToMove(), true)) { // Only check for peices of the color whose turn it is to move
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
            for (int i = 0; i < precomputedMoveData.getTilesToEdge()[startTile][direction]; i++) {

                int endTile = startTile + precomputedMoveData.slidingOffsets[direction] * (i + 1); // Get the legal ending tile for the piece. Multiply by (i + 1) because sliding pieces can move an infinite distance in each of their directions
                int pieceOnEndTile = boardToUse.getTile()[endTile]; // Figure out if there is a piece on the ending tile

                // If the piece on the ending tile is of the same color, the move is illegal
                if (Piece.checkColor(pieceOnEndTile, boardToUse.getFriendlyColor(), true)) { break; }

                // Add the move to the list of legal moves
                movesGenerated.add(new Move(startTile, endTile));

                // If the piece on the ending tile is of the opposing color, you can capture, but not go past it
                if (Piece.checkColor(pieceOnEndTile, boardToUse.getOpponentColor(), true)) { break; }

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

        // Regular king moves
        for (int direction = 0; direction < 8; direction++) {
            // Make sure there are available tiles in this direction
            if (precomputedMoveData.getTilesToEdge()[startTile][direction] == 0) { continue; }

            int offset = precomputedMoveData.slidingOffsets[direction];
            int endTile = startTile + offset;
            int pieceOnEndTile = boardToUse.getTile()[endTile]; // Figure out if there is a piece on the ending tile

            // If the piece on the ending tile is of the same color, the move is illegal
            if (Piece.checkColor(pieceOnEndTile, boardToUse.getFriendlyColor(), true)) { continue; }

            // Add the move to the list of legal moves
            movesGenerated.add(new Move(startTile, endTile));
        }

        // Castling moves
        for (int castleDir = 0; castleDir < 2; castleDir++) {
            int offset = precomputedMoveData.castlingOffsets[castleDir];
            int endTile = startTile + offset;

            // White castles
            if (boardToUse.getColorToMove() == 0 && !(Chess.getBoard().getTilesOpponentControls().contains(Chess.getBoard().getKings()[Chess.getBoard().getColorToMove()].getTilesWithPieces()[0]))) { // Prevent castling out of check
                // White queenside is allowed and all tiles are clear
                if (offset == -2 && boardToUse.getCastleQ() && boardToUse.getTile()[startTile - 1] == 0 && boardToUse.getTile()[startTile - 2] == 0 && boardToUse.getTile()[startTile - 3] == 0 && !(Chess.getBoard().getTilesOpponentControls().contains(startTile - 1))) {
                    moveFlag = Move.Flag.none;
                    moveFlag = Move.Flag.whiteCastleQueenside;
                    movesGenerated.add(new Move(startTile, endTile, moveFlag));
                }
                // White kingside is allowed and all tiles are clear
                else if (offset == 2 && boardToUse.getCastleK() && boardToUse.getTile()[startTile + 1] == 0 && boardToUse.getTile()[startTile + 2] == 0 && !(Chess.getBoard().getTilesOpponentControls().contains(startTile + 1))) {
                    moveFlag = Move.Flag.none;
                    moveFlag = Move.Flag.whiteCastleKingside;
                    movesGenerated.add(new Move(startTile, endTile, moveFlag));
                }
            }
            // Black castles
            else if (boardToUse.getColorToMove() == 1 && !(Chess.getBoard().getTilesOpponentControls().contains(Chess.getBoard().getKings()[Chess.getBoard().getColorToMove()].getTilesWithPieces()[0]))) { // Prevent castling out of check
                // Black queenside is allowed and all tiles are clear
                if (offset == 2 && boardToUse.getCastleq() && boardToUse.getTile()[startTile + 1] == 0 && boardToUse.getTile()[startTile + 2] == 0 && boardToUse.getTile()[startTile + 3] == 0 && !(Chess.getBoard().getTilesOpponentControls().contains(startTile + 1))) {
                    moveFlag = Move.Flag.none;
                    moveFlag = Move.Flag.blackCastleQueenside;
                    movesGenerated.add(new Move(startTile, endTile, moveFlag));
                }
                // Black kingside is allowed and all tiles are clear
                else if (offset == -2 && boardToUse.getCastlek() && boardToUse.getTile()[startTile - 1] == 0 && boardToUse.getTile()[startTile - 2] == 0 && !(Chess.getBoard().getTilesOpponentControls().contains(startTile - 1))) {
                    moveFlag = Move.Flag.none;
                    moveFlag = Move.Flag.blackCastleKingside;
                    movesGenerated.add(new Move(startTile, endTile, moveFlag));
                }
            }
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
        List<Byte> knightOffsets = precomputedMoveData.getKnightOffsets().get(startTile);

        for (int endTile : knightOffsets) {
            int pieceOnEndTile = boardToUse.getTile()[endTile]; // Figure out if there is a piece on the ending tile

            // If the piece on the ending tile is of the same color, the move is illegal
            if (Piece.checkColor(pieceOnEndTile, boardToUse.getFriendlyColor(), true)) { continue; }

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
        pawnOffsets.add(8 * boardToUse.getPawnDir());
        List<Integer> edgeTilesLeft = (boardToUse.getPawnDir() == -1) ? Arrays.asList(0, 8, 16, 24, 32, 40, 48, 56) : Arrays.asList(7, 15, 23, 31, 39, 47, 55, 63), edgeTilesRight = (boardToUse.getPawnDir() == -1) ? Arrays.asList(7, 15, 23, 31, 39, 47, 55, 63): Arrays.asList(0, 8, 16, 24, 32, 40, 48, 56); // Check to make sure a pawn isn't capturing by wrapping around the board

        if (precomputedMoveData.pawnStartingTiles.contains(startTile)) { // Double pawn push
            pawnOffsets.add(16 * boardToUse.getPawnDir());
        }

        for (int direction = 0; direction < pawnOffsets.size(); direction++) {
            int endTile = startTile + pawnOffsets.get(direction); // Get the legal ending tile for the piece. Multiply by (i + 1) because sliding pieces can move an infinite distance in each of their directions
            if (endTile < 0 || endTile > 63) { continue; }

            int pieceUp8 = ((startTile + (8 * boardToUse.getPawnDir())) < 64 && (startTile + (8 * boardToUse.getPawnDir())) >= 0) ? boardToUse.getTile()[startTile + (8 * boardToUse.getPawnDir())] : 0; // Piece directly in front of the pawn
            int pieceUp16 = ((startTile + (16 * boardToUse.getPawnDir())) < 64 && (startTile + (16 * boardToUse.getPawnDir())) >= 0) ? boardToUse.getTile()[startTile + (16 * boardToUse.getPawnDir())] : 0; // Piece two tiles up from the pawn
            int capturablePieceOne = (pawnOffsets.get(direction) == 8 * boardToUse.getPawnDir()) ? ((boardToUse.getPawnDir() == -1) ? ((endTile - 1 >= 0) ? boardToUse.getTile()[endTile - 1] : 0) : ((endTile + 1 < 64) ? boardToUse.getTile()[endTile + 1] : 0)) : 0; // Piece that the pawn can capture to one side
            int capturablePieceTwo = (pawnOffsets.get(direction) == 8 * boardToUse.getPawnDir()) ? ((boardToUse.getPawnDir() == -1) ? ((endTile + 1 < 64) ? boardToUse.getTile()[endTile + 1] : 0) : ((endTile - 1 >= 0) ? boardToUse.getTile()[endTile - 1] : 0)) : 0; // Piece that the pawn can capture to the other side
            int enPassantPiece = (enPassantTile != -1) ? boardToUse.getTile()[enPassantTile - (8 * boardToUse.getPawnDir())] : 0; // Piece that the pawn can en passant capture

            if (pieceUp8 != 0) {
                pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(16 * boardToUse.getPawnDir()))); // Remove the option to move two squares and jump over the piece in front
                pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(8 * boardToUse.getPawnDir()))); // Remove the option to move one square
            }
            if (pieceUp16 != 0) {
                pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(16 * boardToUse.getPawnDir()))); // Allow the pawn to move one square, but prevent it from moving two squares and landing on a piece
            }

            // En passant captures
            if (enPassantPiece != 0) {
                if (enPassantTile == (startTile + (9 * boardToUse.getPawnDir())) && !edgeTilesRight.contains((startTile + (9 * boardToUse.getPawnDir())))) {
                    pawnOffsets.add(9 * boardToUse.getPawnDir());
                    movesGenerated.addAll(generateEnPassantCaptures(startTile));
                }
                if (enPassantTile == (startTile + (7 * boardToUse.getPawnDir())) && !edgeTilesLeft.contains((startTile + (7 * boardToUse.getPawnDir())))) {
                    pawnOffsets.add(7 * boardToUse.getPawnDir());
                    movesGenerated.addAll(generateEnPassantCaptures(startTile));
                }
            }

            // Regular captures
            if (Piece.checkColor(capturablePieceOne, boardToUse.getOpponentColor(), true) || Piece.checkColor(capturablePieceTwo, boardToUse.getOpponentColor(), true)) {
                if (Piece.checkColor(capturablePieceOne, boardToUse.getOpponentColor(), true) && !edgeTilesLeft.contains(endTile)) {
                    pawnOffsets.add(9 * boardToUse.getPawnDir());
                }
                if (Piece.checkColor(capturablePieceTwo, boardToUse.getOpponentColor(), true) && !edgeTilesRight.contains(endTile)) {
                    pawnOffsets.add(7 * boardToUse.getPawnDir());
                }

                movesGenerated.addAll(generatePawnCaptures(startTile));
                continue;
            }

            // Pawn promoted
            moveFlag = Move.Flag.none;
            if (pawnOffsets.size() > 0 && MoveData.pawnPromotionTiles.contains(endTile)) {
                moveFlag = Move.Flag.promoteToQueen; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                moveFlag = Move.Flag.promoteToRook; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                moveFlag = Move.Flag.promoteToKnight; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                moveFlag = Move.Flag.promoteToBishop; movesGenerated.add(new Move(startTile, endTile, moveFlag));
                continue;
            }

            // Pawn moved two forward
            if (pawnOffsets.size() > 0 && pawnOffsets.get(direction) == 16 * boardToUse.getPawnDir()) {
                moveFlag = Move.Flag.pawnTwoForward;
            }

            // Add the move to the list of legal moves
            if (pieceUp8 == 0) {
                movesGenerated.add(new Move(startTile, endTile, moveFlag));
            }
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

        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(9 * boardToUse.getPawnDir())));
        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(7 * boardToUse.getPawnDir())));

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
            if (pawnOffset != (9 * boardToUse.getPawnDir()) && pawnOffset != (7 * boardToUse.getPawnDir())) {
                continue;
            }
            int endTile = startTile + pawnOffset; // Find the end tile
            int enPassantPiece = (enPassantTile != -1) ? boardToUse.getTile()[enPassantTile - (8 * boardToUse.getPawnDir())] : 0; // Find the piece being targeted by the en passant move

            moveFlag = Move.Flag.none;
            // End tile is the en passant tile and the piece is another pawn
            if (endTile == enPassantTile && Piece.pieceType(enPassantPiece) == Piece.Pawn) {
                moveFlag = Move.Flag.enPassantCapture; // Mark with EP flag
            }

            movesGenerated.add(new Move(startTile, endTile, moveFlag)); // Save the move
        }

        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(9 * boardToUse.getPawnDir())));
        pawnOffsets.removeAll(new ArrayList<>(Collections.singletonList(7 * boardToUse.getPawnDir())));

        return movesGenerated;
    }
    // end: List<Move> generateEnPassantCaptures

}
