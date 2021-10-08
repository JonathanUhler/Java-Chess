// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// MoveData.java
// Chess
//
// Created by Jonathan Uhler on 4/15/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package move;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class MoveData
//
// Initialized some precomputed move data
//
public class MoveData {

    //                                /    Rook   \/   Bishop   \
    // Move offsets for the 8 dirs  | W   E  N   S  NE  SW NW  SE
    public final int[] slidingOffsets = {-1, 1, -8, 8, -7, 7, -9, 9};

    public final int[] castlingOffsets = {-2, 2};

    public static final List<Integer> pawnPromotionTiles = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
    public final List<Integer> pawnStartingTiles = Arrays.asList(48, 49, 50, 51, 52, 53, 54, 55);

    private List<List<Byte>> knightOffsets = new ArrayList<>();

    private int[][] tilesToEdge;


    // ----------------------------------------------------------------------------------------------------
    // public MoveData
    //
    // Computes some basic information about moves
    //
    public MoveData() {

        int[] allKnightOffsets = {-6, 6, -10, 10, -15, 15, -17, 17};

        tilesToEdge = new int[64][];

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                int tile = (row * 8) + col; // Current tile index

                int tilesN = row; // Number of available tiles to the north
                int tilesE = 7 - col; // Number of available tiles to the east
                int tilesS = 7 - row; // Number of available tiles to the south
                int tilesW = col; // Number of available tiles to the west

                // Create a 2d array of each tile and how many tiles are available in each direction
                // Order for directions is N, E, S, W, NE, SW, NW, SE
                // Indexes for directions: 0  1  2  3  4   5   6   7
                // For example, tilesToEdge[0][2] would be "how many tiles south of a1 on the board" which should = 0
                tilesToEdge[tile] = new int[]{
                        tilesW,
                        tilesE,
                        tilesN,
                        tilesS,
                        Math.min(tilesN, tilesE),
                        Math.min(tilesS, tilesW),
                        Math.min(tilesN, tilesW),
                        Math.min(tilesS, tilesE)
                };


                // Knight moves (prevent wrap around)
                List<Byte> legalKnightMoves = new ArrayList<>();

                // For each value in the options for knight move offsets
                for (int knightMoveDelta : allKnightOffsets) {
                    int knightMoveTile = tile + knightMoveDelta; // Find the tile being moved to based on each offset

                    if (knightMoveTile >= 0 && knightMoveTile < 64) { // Make sure this doesn't go off the board (north or south)
                        int knightTileRow = knightMoveTile / 8; // Find the row and col for the tile
                        int knightTileCol = knightMoveTile - knightTileRow * 8;
                        // Ensure knight has moved max of 2 squares on x/y axis (to reject indices that have wrapped around side of board)
                        int maxCoordMoveDst = Math.max(Math.abs(col - knightTileCol), Math.abs(row - knightTileRow));

                        if (maxCoordMoveDst == 2) { // If the knight moved the correct amount (didn't go off the board east or west) the move is legal
                            legalKnightMoves.add((byte) knightMoveTile);
                        }
                    }
                }

                knightOffsets.add(tile, legalKnightMoves);

            }
        }

    }
    // end: public MoveData


    // ====================================================================================================
    // GET methods
    public List<List<Byte>> getKnightOffsets() {
        return knightOffsets;
    }

    public int[][] getTilesToEdge() {
        return tilesToEdge;
    }
    // end: GET methods

}
