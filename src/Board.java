// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Board.java
// Chess
//
// Created by Jonathan Uhler on 3/25/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Board
//
// Keeps track of the board and pieces on the board. Handles moving pieces and and updating the boardstate
//
public class Board implements Cloneable {

    public final File chessProjectPath = new File("./").getAbsoluteFile().getParentFile().getParentFile(); // Get the path for .../Chess/

    public final int whiteIndex = 0; // Index for white pieces in arrays (such as PieceTracker arrays below) of white pieces
    public final int blackIndex = 1; // Index for black pieces in arrays of black pieces
    public int[] tile = new int[64]; // Every tile on the board

    public PieceTracker[] pawns; // 1 PieceTracker for all white pawns, 1 PieceTracker for all black pawns
    public PieceTracker[] knights; // 1 PieceTracker for all white knights, 1 PieceTracker for all black knights
    public PieceTracker[] bishops; // 1 PieceTracker for all white bishops, 1 PieceTracker for all black bishops
    public PieceTracker[] rooks; // 1 PieceTracker for all white rooks, 1 PieceTracker for all black rooks
    public PieceTracker[] queens; // 1 PieceTracker for all white queens, 1 PieceTracker for all black queens
    public PieceTracker[] kings; // 2 integers. 1 for the tile of the white king, 1 for the tile of the black king
    public PieceTracker[] allPieceTrackers; // Every piece for both white and black

    PieceTracker getPieceTracker(int pieceType, int pieceColor) {
        return allPieceTrackers[pieceColor * 8 + pieceType]; // Get a specific piece tracker given only the piece type and color
    }

    public int fullmoves; // Number of fullmoves played this game
    public int fiftyMoveRule; // Number of fullmoves since the last pawn movement or piece capture
    public String enPassantTile = "-"; // Tile for en passant
    public int enPassantCol = -1; // Column for en passant
    public boolean castleK; // White castle kingside
    public boolean castleQ; // White castle queenside
    public boolean castlek; // Black castle kingside
    public boolean castleq; // Black castle queenside

    public String currentFenPosition; // Current arrangement of pieces on the board in fen notation
    public HashMap<String, Integer> threeFoldRepetition = new HashMap<>(); // List of positions and how many times they have appeared in the game

    public List<Integer> tilesOpponentControls = new ArrayList<>();

    public boolean whitesMove; // Is white to move?
    public int colorToMove; // Which color is to move
    public int opponentColor; // What is the opposing color
    public int friendlyColor; // What is the safe color
    public int pawnDir = -1; // What direction do pawns move (up or down)?

    public boolean showLegalMoves = true; // Should legal moves be highlighted?


    // ====================================================================================================
    // public void loadPosition
    //
    // Loads a given position into PieceTrackers that will be used to place the pieces onto the board
    //
    // Arguments--
    //
    // fen:     the FEN string to load
    //
    // Returns--
    //
    // None
    //
    public void loadPosition(String fen) {
        currentFenPosition = fen.split(" ")[0]; // Save the current fen position

        initBoard(); // Initialize the board

        // Get the FEN info for the position being loaded
        FenInfo loadedPosition = FenUtility.loadPositionFromFen(fen);

        // Loop over every tile of the board
        for (int tileIndex = 0; tileIndex < 64; tileIndex++) {
            int piece = loadedPosition.tiles[tileIndex]; // Get the piece on the current tile (note this might be type Piece.None, this is handled later)
            tile[tileIndex] = piece; // Save the current piece to a global array of the tiles on the board

            // Make sure the current tile has a piece
            if (piece != Piece.None) {
                int pieceType = Piece.pieceType(piece);
                int pieceColorIndex = (Piece.checkColor(piece, Piece.White, false)) ? whiteIndex : blackIndex;

                if (pieceType == Piece.Queen) {
                    queens[pieceColorIndex].addPieceToTile(tileIndex);
                }
                else if (pieceType == Piece.Rook) {
                    rooks[pieceColorIndex].addPieceToTile(tileIndex);
                }
                else if (pieceType == Piece.Bishop) {
                    bishops[pieceColorIndex].addPieceToTile(tileIndex);
                }
                else if (pieceType == Piece.Knight) {
                    knights[pieceColorIndex].addPieceToTile(tileIndex);
                }
                else if (pieceType == Piece.Pawn) {
                    pawns[pieceColorIndex].addPieceToTile(tileIndex);
                }
                else if (pieceType == Piece.King) {
                    kings[pieceColorIndex].addPieceToTile(tileIndex);
                }
            }
        }

        whitesMove = loadedPosition.whiteToMove; // Figure out if it is white's turn to move
        colorToMove = (whitesMove) ? 0 : 1; // If it is white's move, then the color to move is white, otherwise it must be black
        opponentColor = (whitesMove) ? 1 : 0;
        friendlyColor = (opponentColor == 0) ? 1 : 0;

        // Castling legality
        castleK = loadedPosition.whiteCastleKingside;
        castleQ = loadedPosition.whiteCastleQueenside;
        castlek = loadedPosition.blackCastleKingside;
        castleq = loadedPosition.blackCastleQueenside;

        // En passant tile
        enPassantTile = loadedPosition.enPassantTile;
        enPassantCol = loadedPosition.enPassantCol;

        // Update fifty move rule and fullmoves count
        fiftyMoveRule = loadedPosition.halfmoves; // Update the number of halfmoves
        fullmoves = loadedPosition.fullmoves; // Update the number of fullmoves
    }
    // end: public void loadPosition


    // ====================================================================================================
    // void initBoard
    //
    // Initializes information about the state of the board and the pieces (including PieceTrackers)
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // None
    //
    void initBoard() {
        // Initialize some basic information about the game
        tile = new int[64];

        // Create the lists of each piece     Pieces for white                                                       Pieces for black
        knights = new PieceTracker[]    { new PieceTracker(10, 0b01000, 0b00010), new PieceTracker(10, 0b10000, 0b00010) };
        pawns = new PieceTracker[]      { new PieceTracker(8, 0b01000, 0b00001),  new PieceTracker(8, 0b10000, 0b00001)  };
        rooks = new PieceTracker[]      { new PieceTracker(10, 0b01000, 0b00100), new PieceTracker(10, 0b10000, 0b00100) };
        bishops = new PieceTracker[]    { new PieceTracker(10, 0b01000, 0b00011), new PieceTracker(10, 0b10000, 0b00011) };
        queens = new PieceTracker[]     { new PieceTracker(9, 0b01000, 0b00110),  new PieceTracker(9, 0b10000, 0b00110)  };
        kings = new PieceTracker[]      { new PieceTracker(1, 0b01000, 0b00111),  new PieceTracker(1, 0b10000, 0b00111)  };
        PieceTracker spacer = new PieceTracker(0, 0, 0); // Spacers are used because of the bit-values of each piece. Some indices of the allPieceTracker array are impossible to get to

        // Every piece on the board
        allPieceTrackers = new PieceTracker[] {
            // White
            spacer,
            pawns[whiteIndex],
            knights[whiteIndex],
            bishops[whiteIndex],
            rooks[whiteIndex],
            spacer,
            queens[whiteIndex],
            kings[whiteIndex],
            // Black
            spacer,
            pawns[blackIndex],
            knights[blackIndex],
            bishops[blackIndex],
            rooks[blackIndex],
            spacer,
            queens[blackIndex],
            kings[blackIndex],
        };
    }
    // end: void initBoard


    // ====================================================================================================
    // public void makeMove
    //
    // Makes a move on the board
    //
    // Arguments--
    //
    // move:    the move to be made
    //
    // isGhost: should the move be played on the board, or is this move just for check(mate) search
    //
    // Returns--
    //
    // None
    //
    public void makeMove(Move move, boolean isGhost) {
        int moveFrom = move.startTile(); // Tile the piece starts on
        int moveTo = move.endTile(); // Tile the piece goes to

        int movePiece = tile[moveFrom]; // The 5-bit color | type of the piece on the starting tile
        int movePieceType = Piece.pieceType(movePiece); // The type (single digit integer 0-7) of the piece
        int capturedPiece = tile[moveTo];
        int capturedPieceType = Piece.pieceType(capturedPiece); // The type (single digit integer 0-7) of any pieces captured by the moving piece
        int enPassantCapturedPiece = (moveTo + 8 < 64) ? tile[moveTo + 8] : 0;
        int enPassantCapturedType = Piece.pieceType(enPassantCapturedPiece);
        int pieceGoingToEndTile = movePiece; // The 5-bit color | type of the piece going to the end tile

        int moveFlag = move.moveFlag();
        boolean isPromotion = move.isPromotion();
        boolean isEnPassant = (moveFlag == Move.Flag.enPassantCapture);
        boolean isCastle = move.isCastle();

        // Make sure the end tile is on the board
        if (moveFrom > 63 || moveTo > 63) {
            return;
        }

        // Make sure a real piece was moved
        if (movePiece == 0) {
            return;
        }

        // Update halfmoves and fullmoves
        fiftyMoveRule++;
        if (!whitesMove) { fullmoves++; }

        // Handle regular captures
        if (capturedPieceType != 0) {
            fiftyMoveRule = 0; // Reset the 50 move rule counter

            // If a piece is captured, remove it
            getPieceTracker(capturedPieceType, opponentColor).removePieceFromTile(moveTo); // 0 = white, 1 = black

            if (!isGhost) {
                try { BoardManager.playSound(chessProjectPath + "/reference/sounds/capture.wav"); } // Play the capture sound
                catch (LineUnavailableException | IOException | UnsupportedAudioFileException exception) { exception.printStackTrace(); } // Gracefully handle any possible exceptions from the capture sound failing
            }
        }

        // Handle en passant pawn capture
        if (moveFlag == Move.Flag.enPassantCapture) {
            fiftyMoveRule = 0; // Reset the 50 move rule counter

            // If a piece is captured, remove it
            getPieceTracker(enPassantCapturedType, opponentColor).removePieceFromTile(moveTo + 8); // 0 = white, 1 = black
            tile[moveTo + 8] = 0;

            if (!isGhost) {
                try { BoardManager.playSound(chessProjectPath + "/reference/sounds/capture.wav"); } // Play the capture sound
                catch (LineUnavailableException | IOException | UnsupportedAudioFileException exception) { exception.printStackTrace(); } // Gracefully handle any possible exceptions from the capture sound failing
            }
        }

        // Handle movement
        getPieceTracker(movePieceType, colorToMove).movePiece(moveFrom, moveTo); // 0 = white, 1 = black

        if (capturedPieceType == 0 && !isPromotion && !isEnPassant && !isCastle) {
            if (!isGhost) {
                try { BoardManager.playSound(chessProjectPath + "/reference/sounds/move.wav"); } // Play the capture sound
                catch (LineUnavailableException | IOException | UnsupportedAudioFileException exception) { exception.printStackTrace(); } // Gracefully handle any possible exceptions from the capture sound failing
            }
        }

        // Handle change in castling legality
        // King moved --> castling rights lost for both sides
        if (movePieceType == Piece.King) {
            if (colorToMove == 0) {
                castleK = false;
                castleQ = false;
            } else {
                castlek = false;
                castleq = false;
            }
        }
        // Rook moved
        else if (movePieceType == Piece.Rook) {
            // Rook on tile 63 moved --> castling rights on right side lost
            if (moveFrom == 63) {
                if (colorToMove == 0) { castleK = false; }
                else { castleq = false; }
            }
            // Rook on tile 56 moved --> castling rights on left side lost
            else if (moveFrom == 56) {
                if (colorToMove == 0) { castleQ = false; }
                else { castlek = false; }
            }
        }
        // The rook on the right side was captured; castling right is no longer allowed
        else if (moveTo == 0) {
            if ((colorToMove ^ 1) == 0) { castleK = false; }
            else { castleq = false; }
        }
        // The rook on the left side was captured; castling left is no longer allowed
        else if (moveTo == 7) {
            if ((colorToMove ^ 1) == 0) { castleQ = false; }
            else { castlek = false; }
        }

        // Handle castling
        if (isCastle) {
            // King castled to the right
            if (moveTo > moveFrom) {
                getPieceTracker(movePieceType, colorToMove).movePiece(moveFrom, moveTo);
                getPieceTracker(Piece.Rook, colorToMove).movePiece(63, moveTo - 1);
                tile[moveTo - 1] = tile[63];
                tile[63] = 0;
            }
            // King castled to the left
            else if (moveTo < moveFrom) {
                getPieceTracker(movePieceType, colorToMove).movePiece(moveFrom, moveTo);
                getPieceTracker(Piece.Rook, colorToMove).movePiece(56, moveTo + 1);
                tile[moveTo + 1] = tile[56];
                tile[56] = 0;
            }

            // Remove the ability to castle a second time
            if (colorToMove == 0) {
                castleK = false;
                castleQ = false;
            } else {
                castlek = false;
                castleq = false;
            }
        }

        // Handle promotion
        if (isPromotion) {
            int promoteType = 0;

            switch (moveFlag) {
                case Move.Flag.promoteToQueen:
                    promoteType = Piece.Queen;
                    queens[colorToMove].addPieceToTile(moveTo);
                    break;
                case Move.Flag.promoteToRook:
                    promoteType = Piece.Rook;
                    rooks[colorToMove].addPieceToTile(moveTo);
                    break;
                case Move.Flag.promoteToBishop:
                    promoteType = Piece.Bishop;
                    bishops[colorToMove].addPieceToTile(moveTo);
                    break;
                case Move.Flag.promoteToKnight:
                    promoteType = Piece.Knight;
                    knights[colorToMove].addPieceToTile(moveTo);
                    break;

            }
            pieceGoingToEndTile = ((colorToMove + 1) * 8) | promoteType; // Update the piece being moved
            pawns[colorToMove].removePieceFromTile(moveTo); // Remove the pawn to later replace it with the promoted piece
        }

        // Update board representation
        tile[moveTo] = pieceGoingToEndTile; // Update the tile array with the new piece
        tile[moveFrom] = 0; // Remove the moved piece from its old location in the tile array

        enPassantTile = "-"; // Reset en passant tile
        enPassantCol = -1; // Reset en passant column

        // Handle pawn moving two forward
        if (moveFlag == Move.Flag.pawnTwoForward) {
            String[] colNames = {"a", "b", "c", "d", "e", "f", "g", "h"};
            enPassantTile = "";
            enPassantTile += colNames[7 - (moveTo % 8)]; // Flag the column of the pawn that moved 2 forward (-7 to make it from the other player's perspective)
            enPassantTile += 7 - (moveTo / 8); // Flag the row of the pawn that moved 2 forward (-7 to make it from the other player's perspective)

            enPassantCol = 7 - (moveTo % 8);
        }

        // If a pawn was moved or a piece was captured, reset the 50 move counter
        if (Piece.pieceType(movePiece) == Piece.Pawn) { fiftyMoveRule = 0; }

        if (!isGhost) {
            if (threeFoldRepetition.containsKey(currentFenPosition)) { // If the current position has already happened, update the number of times it has appeared
                threeFoldRepetition.put(currentFenPosition, threeFoldRepetition.get(currentFenPosition) + 1);
            } else { // Otherwise, add it
                threeFoldRepetition.put(currentFenPosition, 1);
            }
        }
    }
    // end: public void makeMove


    // ====================================================================================================
    // public void changePlayer
    //
    // Changes information about which player is current taking their turn
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // None
    //
    public void changePlayer() {
        // Update whose turn it is to move
        // 0 = white, 1 = black
        whitesMove = !whitesMove;
        colorToMove ^= 1;
        opponentColor ^= 1;
        friendlyColor ^= 1;
    }
    // public void changePlayer


    // ====================================================================================================
    // public void checkState
    //
    // Checks the state of the game to determine how many legal moves the current player has and if they
    // are in check
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // None
    //
    public void checkState() {
        // Figure out if the player is in check
        boolean inCheck = false;
        if (tilesOpponentControls.contains(kings[colorToMove].tilesWithPieces[0])) { inCheck = true; }

        // Figure out how many legal moves the player has
        int numLegalMoves = new LegalMoveUtility().allLegalMoves().size();

        // Update the game state (check for draw or win/loss)
        GameStateUtility.actOnGameState(numLegalMoves, inCheck);
    }
    // end: public void checkState


    // ====================================================================================================
    // public Object clone
    //
    // Creates a deep copy of the current Board object into a new Board object
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // newBoard:    the deep copy
    //
    @Override public Object clone() throws CloneNotSupportedException {
        Board newBoard = (Board) super.clone();

        newBoard.pawns = new PieceTracker[] {
                (PieceTracker) this.pawns[0].clone(),
                (PieceTracker) this.pawns[1].clone()
        };
        newBoard.knights = new PieceTracker[] {
                (PieceTracker) this.knights[0].clone(),
                (PieceTracker) this.knights[1].clone()
        };
        newBoard.bishops = new PieceTracker[] {
                (PieceTracker) this.bishops[0].clone(),
                (PieceTracker) this.bishops[1].clone()
        };
        newBoard.rooks = new PieceTracker[] {
                (PieceTracker) this.rooks[0].clone(),
                (PieceTracker) this.rooks[1].clone()
        };
        newBoard.queens = new PieceTracker[] {
                (PieceTracker) this.queens[0].clone(),
                (PieceTracker) this.queens[1].clone()
        };
        newBoard.kings = new PieceTracker[] {
                (PieceTracker) this.kings[0].clone(),
                (PieceTracker) this.kings[1].clone()
        };

        PieceTracker spacer = new PieceTracker(0, 0, 0);
        newBoard.allPieceTrackers = new PieceTracker[] {
                // White
                spacer,
                newBoard.pawns[newBoard.whiteIndex],
                newBoard.knights[newBoard.whiteIndex],
                newBoard.bishops[newBoard.whiteIndex],
                newBoard.rooks[newBoard.whiteIndex],
                spacer,
                newBoard.queens[newBoard.whiteIndex],
                newBoard.kings[newBoard.whiteIndex],
                // Black
                spacer,
                newBoard.pawns[newBoard.blackIndex],
                newBoard.knights[newBoard.blackIndex],
                newBoard.bishops[newBoard.blackIndex],
                newBoard.rooks[newBoard.blackIndex],
                spacer,
                newBoard.queens[newBoard.blackIndex],
                newBoard.kings[newBoard.blackIndex],
        };

        newBoard.tile = this.tile.clone();

        return newBoard;
    }
    // end: public Object clone

}
// end: public class Board