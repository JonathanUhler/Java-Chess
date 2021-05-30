// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// LegalMoveUtility.java
// Chess
//
// Created by Jonathan Uhler on 5/11/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import java.util.ArrayList;
import java.util.List;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class LegalMoveUtility
//
// Handles legal move generation
//
public class LegalMoveUtility {

    // ====================================================================================================
    // public List<Move> allLegalMoves
    //
    // Generates all the legal moves for the current player
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // legalMoves:  a list of all the legal moves for the current player
    public List<Move> allLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();

        // Generate pseudo-legal moves
        MoveUtility checkMyMoves = new MoveUtility();
        List<Move> pseudoLegalMoves = checkMyMoves.generateMoves(board);

        // For each of my possible moves...
        for (Move moveToVerify : pseudoLegalMoves) {
            // Make my move on the ghost board deep copy
            // Deep copy the real board
            Board ghostBoard = new Board();
            try { ghostBoard = (Board) board.clone(); }
            catch (CloneNotSupportedException cloneNotSupportedException) { cloneNotSupportedException.printStackTrace(); }

            ghostBoard.pawnDir = -1;
            ghostBoard.makeMove(moveToVerify, true);
            ghostBoard.changePlayer();

            // Generate all of the opponent's responses to my single move
            MoveUtility checkTheirMoves = new MoveUtility();
            ghostBoard.pawnDir = 1;
            List<Move> opponentResponses = checkTheirMoves.generateMoves(ghostBoard);
            // Create a list of the tiles my opponent is attacking
            List<Integer> opponentAttackedTiles = new ArrayList<>(MoveUtility.returnEndingTiles(opponentResponses));

            // If the opponent is attacking...            king...       of my color...
            if (opponentAttackedTiles.contains(ghostBoard.kings[Chess.board.colorToMove].tilesWithPieces[0])) {
                continue; // ...my move was illegal
            }
            else {
                legalMoves.add(moveToVerify); // ...my move was legal
            }
        }

        return legalMoves;
    }
    // end: public List<Move> allLegalMoves

}
// end: public class LegalMoveUtility