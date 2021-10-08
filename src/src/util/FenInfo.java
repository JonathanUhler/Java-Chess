package util;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class FenInfo
//
// Information about the loaded FEN string
//
public class FenInfo {
    public int[] tiles; // Internal memory for every tile that makes up the chessboard

    public boolean whiteCastleKingside;
    public boolean whiteCastleQueenside;
    public boolean blackCastleKingside;
    public boolean blackCastleQueenside;

    public String enPassantTile; // Which tile is open for en passant
    public int enPassantCol; // Which column is open for en passant
    public int halfmoves; // Number of halfmoves this game
    public int fullmoves; // Number of fullmoves this game

    public boolean whiteToMove; // Is it white's turn. If not, then it must be black's turn


    // ----------------------------------------------------------------------------------------------------
    // public FenInfo
    //
    // Sets the number of tiles to 64
    //
    public FenInfo() {
        tiles = new int[64];
    }
    // end: public FenInfo
}
// end: public class FenInfo