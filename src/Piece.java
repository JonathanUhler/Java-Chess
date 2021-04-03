// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// GameManager.java
// Chess
//
// Created by Jonathan Uhler on 3/25/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Piece
//
// Framework to create instances of each piece
//
public class Piece {

    // Piece type is the last 3 bits of a 5-bit number
    // Piece color is the first 2 bits of a 5-bit number

    public static final int None = 0; // 0b__000
    public static final int Pawn = 1; // 0b__001
    public static final int Knight = 2; // 0b__010
    public static final int Bishop = 3; // 0b__011
    public static final int Rook = 4; // 0b__100
    public static final int Queen = 6; // 0b__110
    public static final int King = 7; // 0b__111

    public static final int White = 8; // 0b01___
    public static final int Black = 16; // 0b10___

    static final int typeAnd = 0b00111; // Maximum value of a piece type
    static final int colorAnd = 0b01000 | 0b10000; // Combination of white and black value


    // ====================================================================================================
    // public static boolean findColor
    //
    // Finds the color of a piece using bitwise AND
    //
    // Arguments--
    //
    // piece:   the piece to find the color of
    //
    // color:   this is usually Piece.White. The purpose of this function is to compare color against colorAnd
    //          and return the result
    //
    // Returns--
    //
    // The color of the piece
    public static boolean findColor (int piece, int color) { return (piece & colorAnd) == color; }
    // end: public static boolean findColor


    // ====================================================================================================
    // public static int pieceType
    //
    // Finds the type of piece
    //
    // Arguments--
    //
    // piece:   the piece to find the type of
    //
    // Returns--
    //
    // The type of the piece
    public static int pieceType (int piece) {
        return piece & typeAnd;
    }
    // end: public static int pieceType


    public static boolean isRookQueen (int piece) {
        return (piece & 0b110) == 0b110;
    }

    public static boolean isBishopQueen (int piece) {
        return (piece & 0b101) == 0b101;
    }

}
// end: public class Piece
