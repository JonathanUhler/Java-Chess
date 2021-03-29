// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// PieceTracker.java
// Chess
//
// Created by Jonathan Uhler on 3/27/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class PieceTracker
//
// Keeps track of all pieces (adding pieces to the board, removing pieces, changing piece location)
//
public class PieceTracker {

    public int[] tilesWithPieces; // A list of tiles that already have pieces on them
    public int[] tileMap = new int[64]; // Allows the ability to transition from a tile's index to the index of that tile in the tilesWithPieces array (eg, given tileIndex, where does tileIndex correspond in the tilesWithPieces array?)
    int pieceCount; // Number of existing pieces
    int maxPieces; // The maximum number of pieces of this type


    // +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
    // public PieceTracker
    //
    // Facilitates the creation of a new PieceTracker for a give type of piece
    //
    // Arguments--
    //
    // maxCountPerPieceType:    the maximum amount of a given type of piece possible in a game
    //
    public PieceTracker (int maxCountPerPieceType) {
        maxPieces = maxCountPerPieceType;
        tilesWithPieces = new int[maxCountPerPieceType];
        tileMap = new int[64];
        pieceCount = 0;
    }
    // end: public PieceTracker


    // ====================================================================================================
    // public void addPieceToTile
    //
    // Adds a new piece to a given tile
    //
    // Arguments--
    //
    // tile:    the index of the tile to add a piece to
    //
    // Returns--
    //
    // None
    //
    public void addPieceToTile(int tile) {
        tilesWithPieces[pieceCount] = tile; // At the last element of tilesWithPieces, add in the new tile as a tile now occupied by a piece
        tileMap[tile] = pieceCount; // Used later to remove or move pieces. The tile given to this function is now related to its index (pieceCount) in the tilesWithPieces array
        pieceCount++; // Increase the total number of pieces
    }
    // end: public void addPieceToTile


    // ====================================================================================================
    // public void removePieceFromTile
    //
    // Removes an existing piece form a given tile
    //
    // Arguments--
    //
    // tile:    the index of the tile to remove a piece from
    //
    // Returns--
    //
    // None
    //
    public void removePieceFromTile(int tile) {
        int pieceIndex = tileMap[tile]; // Fetch the index in tilesWithPieces from the tileMap
        tilesWithPieces[pieceIndex] = tilesWithPieces[pieceCount - 1]; // Change the index of the piece being removed to the last element of the array to fill in gaps
        tileMap[tilesWithPieces[pieceIndex]] = pieceIndex; // After the gap has been filled, make sure to update the location of the piece that filled said gap in the tileMap
        pieceCount--; // Decrease the total number of pieces
    }
    // end: public void removePieceFromTile


    // ====================================================================================================
    // public void movePiece
    //
    // Moves a piece from a starting tile to an ending tile
    //
    // Arguments--
    //
    // startingTile:    the tile index the piece is currently on
    //
    // endingTile:      the tile index the piece should move to
    //
    // Returns--
    //
    // None
    //
    public void movePiece(int startingTile, int endingTile) {
        int pieceIndex = tileMap[startingTile]; // Fetch the index in tilesWithPieces from the tileMap using the starting tile
        tilesWithPieces[pieceIndex] = endingTile; // Move the piece
        tileMap[endingTile] = pieceIndex; // Update the tileMap
    }
    // end: public void movePiece

}
// end: public class PieceTracker
