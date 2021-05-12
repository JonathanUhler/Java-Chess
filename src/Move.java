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
    public static final class Flag {
        public static final int none = 0;
        public static final int enPassantCapture = 1;
        public static final int promoteToQueen = 2;
        public static final int promoteToKnight = 3;
        public static final int promoteToRook = 4;
        public static final int promoteToBishop = 5;
        public static final int pawnTwoForward = 6;
        public static final int whiteCastleKingside = 7;
        public static final int whiteCastleQueenside = 8;
        public static final int blackCastleKingside = 9;
        public static final int blackCastleQueenside = 10;
    }
    // end: public static final class MoveFlag


    int moveValue;


    // Bits 0-5 store starting tile
    // Bits 6-11 store ending tile
    // Bits 12-15 store flag for the move
    final int startTileMasker = 0b0000000000111111;
    final int endTileMasker = 0b0000111111000000;


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
        this.moveValue = (startTile | endTile << 6 | flag << 12);
    }
    // end: public Move


    public int startTile() {
        return moveValue & startTileMasker;
    }


    public int endTile() {
        return (moveValue & endTileMasker) >> 6;
    }


    public boolean isPromotion() {
        int flag = moveFlag();
        return flag == Flag.promoteToQueen || flag == Flag.promoteToRook || flag == Flag.promoteToKnight || flag == Flag.promoteToBishop;
    }


    public boolean isCastle() {
        int flag = moveFlag();
        return flag == Flag.whiteCastleKingside || flag == Flag.whiteCastleQueenside || flag == Flag.blackCastleKingside || flag == Flag.blackCastleQueenside;
    }


    public int moveFlag() {
        return moveValue >> 12;
    }

}
// end: public class Move
