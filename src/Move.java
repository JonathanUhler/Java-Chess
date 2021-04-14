// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Move.java
// Chess
//
// Created by Jonathan Uhler on 4/13/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Move
//
// Contains information about a move on the board
//
public class Move {

    // +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
    // public static final class MoveFlag
    //
    // Different flags a move can have
    //
    public static final class MoveFlag {
        public final int none = 0;
        public final int enPassantCapture = 1;
        public final int castled = 2;
        public final int promoteToQueen = 3;
        public final int promoteToKnight = 4;
        public final int promoteToRook = 5;
        public final int promoteToBishop = 6;
        public final int pawnTwoForward = 7;
    }
    // end: public static final class MoveFlag

    final short moveValue;

    // Bits 0-5 store starting tile
    // Bits 6-11 store ending tile
    // Bits 12-15 store flag for the move
    final short startTileMasker = 0b0000000000111111;
    final short endTileMasker = 0b0000111111000000;
    final short flagMasker = (short) 0b1111000000000000;


    // ----------------------------------------------------------------------------------------------------
    // public Move
    //
    // Constructor 1 for Move class
    //
    // Arguments--
    //
    // moveValue:   the value of the move (16-bit binary integer that holds the starting tile, ending tile, and
    //              flag
    //
    public Move (short moveValue) {
        this.moveValue = moveValue;
    }
    // end: public Move


    // ----------------------------------------------------------------------------------------------------
    // public Move
    //
    // Constructor 2 for Move class
    //
    // Arguments--
    //
    // startTile:   the starting tile for the moving piece
    //
    // endTile:     the desired ending tile for the moving piece
    //
    public Move (int startTile, int endTile) {
        this.moveValue = (short) (startTile | endTile << 6);
    }
    // end: public Move


    // ----------------------------------------------------------------------------------------------------
    // public Move
    //
    // Constructor 3 for Move class
    //
    // Arguments--
    //
    // startTile:   the starting tile for the moving piece
    //
    // endTile:     the desired ending tile for the moving piece
    //
    // flag:        special flag for the move
    //
    public Move (int startTile, int endTile, int flag) {
        this.moveValue = (short) (startTile | endTile << 6 | flag << 12);
    }
    // end: public Move


    public int startTile() {
        return moveValue & startTileMasker;
    }

    public int endTile() {
        return (moveValue & endTileMasker) >> 6;
    }

}
// end: public class Move
