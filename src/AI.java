// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// AI.java
// Chess
//
// Created by Jonathan Uhler on 5/20/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import java.time.Duration;
import java.time.LocalTime;
import java.util.*;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class AI
//
// AI/computer player controller
//
public class AI {

    Board ghostBoard = new Board();
    public int movesFound = 0;

    // Rewards and penalties to incentivize a certain style of play
    final double aiCaptureIncentive = 0.1; // Small reward for a move that results in a capture for the AI
    final double positionalPlayIncentive = 0.005; // Multiplier for the values in the tables below
    final double[] pawnPositionIncentives = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 50, 50, 50, 50, 50, 50, 50, 50, 10, 10, 20, 30, 30, 20, 10, 10, 5, 5, 10, 25, 25, 10, 5, 5, 0, 0, 0, 20, 20, 0, 0, 0, 5, -5, -10, 0, 0, -10, 5, 5, 5, 10, 10, -20, -20, 10, 10, 5, 0, 0, 0, 0, 0, 0, 0, 0};
    final double[] knightPositionIncentives = new double[] {-50, -40, -30, -30, -30, -30, -40, -50, -40, -20, 0, 0, 0, 0, -20, -40, -30, 0, 10, 15, 15, 10, 0, -30, -30, 5, 15, 20, 20, 15, 5, -30, -30, 0, 15, 20, 20, 15, 0, -30, -30, 5, 10, 15, 15, 10, 5, -30, -40, -20, 0, 5, 5, 0, -20, -40, -50, -40, -30, -30, -30, -30, -40, -50};
    final double[] bishopPositionIncentives = new double[] {-20, -10, -10, -10, -10, -10, -10, -20, -10, 0, 0, 0, 0, 0, 0, -10, -10, 0, 5, 10, 10, 5, 0, -10, -10, 5, 5, 10, 10, 5, 5, -10, -10, 0, 10, 10, 10, 10, 0, -10, -10, 10, 10, 10, 10, 10, 10, -10, -10, 5, 0, 0, 0, 0, 5, -10, -20, -10, -10, -10, -10, -10, -10, -20};
    final double[] rookPositionIncentives = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 5, 10, 10, 10, 10, 10, 10, 5, -5, 0, 0, 0, 0, 0, 0, -5, -5, 0, 0, 0, 0, 0, 0, -5, -5, 0, 0, 0, 0, 0, 0, -5, -5, 0, 0, 0, 0, 0, 0, -5, -5, 0, 0, 0, 0, 0, 0, -5, 0, 0, 0, 5, 5, 0, 0, 0};
    final double[] queenPositionIncentives = new double[] {-20, -10, -10, -5, -5, -10, -10, -20, -10, 0, 0, 0, 0, 0, 0, -10, -10, 0, 5, 5, 5, 5, 0, -10, -5, 0, 5, 5, 5, 5, 0, -5, 0, 0, 5, 5, 5, 5, 0, -5, -10, 5, 5, 5, 5, 5, 0, -10, -10, 0, 5, 0, 0, 0, 0, -10, -20, -10, -10, -5, -5, -10, -10, -20};
    final double[] kingPositionIncentives = new double[] {-30, -40, -40, -50, -50, -40, -40, -30, -30, -40, -40, -50, -50, -40, -40, -30, -30, -40, -40, -50, -50, -40, -40, -30, -30, -40, -40, -50, -50, -40, -40, -30, -20, -30, -30, -40, -40, -30, -30, -20, -10, -20, -20, -20, -20, -20, -20, -10, 20, 20, 0, 0, 0, 0, 20, 20, 20, 30, 10, 0, 0, 10, 30, 20};


    // ----------------------------------------------------------------------------------------------------
    // public AI
    //
    // AI constructor
    //
    // Arguments--
    //
    // None
    //
    public AI() {
        // Create a deep copy of the real board to use for searching
        try { ghostBoard = (Board) Chess.board.clone(); }
        catch (CloneNotSupportedException cloneNotSupportedException) { cloneNotSupportedException.printStackTrace(); }
    }
    // end: public AI


    // ====================================================================================================
    // double getMoveIncentive
    //
    // Returns a small incentive for good positional play
    //
    // Arguments--
    //
    // piece:       the piece being moved
    //
    // move:        the move made with that piece
    //
    // Returns--
    //
    // incentive:   the reward/punishment making that move would result in
    //
    double getMoveIncentive(int piece, Move move) {
        double incentive;

        switch (Piece.pieceType(piece)) {
            case Piece.Pawn:
                incentive = pawnPositionIncentives[move.endTile()];
                break;
            case Piece.Knight:
                incentive = knightPositionIncentives[move.endTile()];
                break;
            case Piece.Bishop:
                incentive = bishopPositionIncentives[move.endTile()];
                break;
            case Piece.Rook:
                incentive = rookPositionIncentives[move.endTile()];
                break;
            case Piece.Queen:
                incentive = queenPositionIncentives[move.endTile()];
                break;
            case Piece.King:
                incentive = kingPositionIncentives[move.endTile()];
                break;
            default:
                incentive = 0;
                break;
        }

        return  incentive * positionalPlayIncentive;
    }
    // end: double getMoveIncentive


    // ====================================================================================================
    // List<Move> orderMoves
    //
    // Move ordering optimization
    //
    // Arguments--
    //
    // movesToOrder:    the list of moves to order
    //
    // board:           the board the moves are based on
    //
    // Returns--
    //
    // scoredMoves:     the list of ordered moves
    //
    List<Move> orderMoves(List<Move> movesToOrder, Board board) {
        HashMap<Move, Integer> scoredMoves = new HashMap<>();

        for (Move move : movesToOrder) {
            int moveScore;
            int movePiece = Piece.pieceType(board.tile[move.startTile()]);
            int capturedPiece = Piece.pieceType(board.tile[move.endTile()]);

            if (capturedPiece != 0) {
                moveScore = Piece.getValue(capturedPiece) - Piece.getValue(movePiece);
                scoredMoves.put(move, moveScore);
            }
            else {
                scoredMoves.put(move, 0);
            }
        }

        scoredMoves = BoardManager.sortByValue(scoredMoves);

        return new ArrayList<>(scoredMoves.keySet());
    }
    // end: List<Move> orderMoves


    // ====================================================================================================
    // double search -- thanks to M.U. for helping with the search algorithm for this project!
    //
    // Modified minimax search algorithm
    //
    // Arguments--
    //
    // currentLevel:    the current depth of the search
    //
    // maxDepth:        the maximum depth the search is allowed to go to
    //
    // move:            the move to make in this recursive call
    //
    // runningTotal:    the total score for the ai so far
    //
    // Returns--
    //
    // [Recursive] The evaluation of a given node in the tree
    //
    double search(int currentLevel, int maxDepth, Move move, double runningTotal) {

        double incrementalVal = 0;

        // Create deep board copy
        Board unmakeMove = new Board();
        try { unmakeMove = (Board) ghostBoard.clone(); }
        catch (CloneNotSupportedException cloneNotSupportedException) { cloneNotSupportedException.printStackTrace(); }

        // A capture was made
        if (Move.isCapture(move, ghostBoard)) {
            incrementalVal += Piece.getValue(Piece.pieceType(ghostBoard.tile[move.endTile()]));
            // Add capture incentive for the ai
            if (currentLevel % 2 == 1) {
                incrementalVal += aiCaptureIncentive;
            }
        }

        // Add positional incentive for the ai
        if (currentLevel % 2 == 1) {
            incrementalVal += getMoveIncentive(Piece.pieceType(ghostBoard.tile[move.startTile()]), move);
        }

        // Make move
        ghostBoard.makeMove(move, true);

        // Update the incremental value
        if (currentLevel % 2 == 0) { runningTotal -= incrementalVal; } // Human level
        else { runningTotal += incrementalVal; } // AI level

//        System.out.println("start: " + (63 - move.startTile()) + ", end: " + (63 - move.endTile()) + ", level: " + currentLevel + ", running total: " + runningTotal);

        // Increase the level
        currentLevel++;

        if (currentLevel > maxDepth) {
            try { ghostBoard = (Board) unmakeMove.clone(); }
            catch (CloneNotSupportedException cloneNotSupportedException) { cloneNotSupportedException.printStackTrace(); }
            return runningTotal;
        }

        // Generate possible moves
        ghostBoard.changePlayer();
        LegalMoveUtility legalMoveUtility = new LegalMoveUtility();
        List<Move> legalMoves = orderMoves(legalMoveUtility.allLegalMoves(ghostBoard), ghostBoard);
        movesFound += legalMoves.size();

        // Check to see if the current node leads to the end of the game
        if (legalMoves.size() == 0) {
            try { ghostBoard = (Board) unmakeMove.clone(); }
            catch (CloneNotSupportedException cloneNotSupportedException) { cloneNotSupportedException.printStackTrace(); }
            if (Chess.board.playerInCheck()) { return (currentLevel % 2 == 0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY; } // Checkmate
            return 0; // Stalemate
        }

        double bestEval = (currentLevel % 2 == 0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;

        // Recursively play all the possible responses
        for (Move nextMove : legalMoves) {
            double evaluation = search(currentLevel, maxDepth, nextMove, runningTotal);

            bestEval = (currentLevel % 2 == 0) ? Math.min(bestEval, evaluation) : Math.max(bestEval, evaluation);
        }

        try { ghostBoard = (Board) unmakeMove.clone(); }
        catch (CloneNotSupportedException cloneNotSupportedException) { cloneNotSupportedException.printStackTrace(); }
        return bestEval;
    }
    // end: double search


    // ====================================================================================================
    // public static void makeMove
    //
    // Finds and makes an AI move
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // None
    //
    public static void makeMove() {
        LegalMoveUtility aiMoveUtil = new LegalMoveUtility();
        List<Move> aiMoves = aiMoveUtil.allLegalMoves(Chess.board);

        // Best eval
        Move bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        // Diagnostics for the evaluation
        int totalMoves = 0;
        LocalTime start = LocalTime.now();

        // Evaluate all the ai's responses
        for (Move aiMove : aiMoves) {
            AI ai = new AI();
            double aiMoveScore = ai.search(1, 4, aiMove, 0.0);

            totalMoves += ai.movesFound;

            if (aiMoveScore >= bestScore) {
                bestScore = aiMoveScore;
                bestMove = aiMove;
            }

            if (bestMove != null) {
                System.out.println("current: " + aiMoveScore + ", best: " + bestScore + ", current move start: " + (63 - aiMove.startTile()) + ", current move end: " + (63 - aiMove.endTile()) + ", best move start: " + (63 - bestMove.startTile()) + ", best move end: " + (63 - bestMove.endTile()));
            }
        }

        LocalTime end = LocalTime.now();

        if (bestMove != null) {
            BoardManager.debugMessage("Total moves evaluated: " + totalMoves + ", best capture score: " + bestScore + ", time: " + Duration.between(start, end).toMillis() + "ms");
            Chess.board.makeMove(bestMove, false);
            Chess.graphics.postMoveUpdates();
            Chess.graphics.drawBoard(new ArrayList<>(Arrays.asList(63 - bestMove.startTile(), 63 - bestMove.endTile())));
        }
    }
    // end: public static void makeMove

}
// end: public class AI