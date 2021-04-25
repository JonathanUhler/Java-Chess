// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Board.java
// Chess
//
// Created by Jonathan Uhler on 3/25/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Board
//
// Sets up the application frame and creates the 8x8 chessboard
//
public class Board {

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

    public String currentFenPosition; // Current arrangement of pieces on the board in fen notation
    public HashMap<String, Integer> threeFoldRepetition = new HashMap<>(); // List of positions and how many times they have appeared in the game

    public boolean whitesMove; // Is white to move?
    public int colorToMove; // Which color is to move
    public int opponentColor; // What is the opposing color
    public int friendlyColor; // What is the safe color
    public boolean whiteOnBottom = true; // Did the white player start with pieces on the bottom of the board?

    public boolean showLegalMoves = true; // Should legal moves be highlighted?

    // Bits 0-3 store white and black kingside/queenside castling legality. 1 = castling allowed, 0 = no castling allowed
    // Bits 4-7 store row of en passant tile (starting at 1, so 0 = no en passant row)
    // Bits 8-13 captured piece
    // Bits 14-... fifty mover counter
    int currentGameState;


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

        // Create game state
        // Castling priorities
        int whiteCastle = ((loadedPosition.whiteCastleKingside) ? 1 << 0 : 0) | ((loadedPosition.whiteCastleQueenside) ? 1 << 1 : 0);
        int blackCastle = ((loadedPosition.blackCastleKingside) ? 1 << 2 : 0) | ((loadedPosition.blackCastleQueenside) ? 1 << 3 : 0);
        // En passant availability
        int epState = loadedPosition.enPassantRow << 4;
        // Current game state
        currentGameState = (short) (whiteCastle | blackCastle | epState); // Update the current game state
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
    public boolean makeMove(Move move, boolean isGhost) {
        int moveFrom = move.startTile(); // Tile the piece starts on
        int moveTo = move.endTile(); // Tile the piece goes to

        int movePiece = tile[moveFrom]; // The 5-bit color | type of the piece on the starting tile
        int movePieceType = Piece.pieceType(movePiece); // The type (single digit integer 0-7) of the piece
        int capturedPiece = tile[moveTo];
        int capturedPieceType = Piece.pieceType(capturedPiece); // The type (single digit integer 0-7) of any pieces captured by the moving piece
        int pieceGoingToEndTile = movePiece; // The 5-bit color | type of the piece going to the end tile

        int moveFlag = move.MoveFlag();
        boolean isPromotion = move.isPromotion();
        boolean isEnPassant = moveFlag == Move.Flag.enPassantCapture;

        // Generate legal moves
        MoveUtility checkMoves = new MoveUtility();
        List<Short> legalMoves = checkMoves.generateMoves();

        // Make sure the end tile is on the board
        if (moveFrom > 63 || moveTo > 63) {
            return false;
        }

        // If the move being played was not found, it is illegal
        if (!legalMoves.contains(move.moveValue)) {
            Chess.graphics.drawPosition(); // Redraw the board
            Chess.graphics.drawBoard(null); // Redraw the board
            return false;
        }

        // Update halfmoves and fullmoves
        fiftyMoveRule++;
        if (!whitesMove) { fullmoves++; }

        // Handle captures
        if (capturedPieceType != 0) {
            fiftyMoveRule = 0; // Reset the 50 move rule counter

            // If a piece is captured, remove it
            getPieceTracker(capturedPieceType, opponentColor).removePieceFromTile(moveTo); // 0 = white, 1 = black
            try {
                BoardManager.playSound(chessProjectPath + "/reference/sounds/capture.wav"); // Play the capture sound
            } catch (LineUnavailableException | IOException | UnsupportedAudioFileException exception) {
                exception.printStackTrace(); // Gracefully handle any possible exceptions from the capture sound failing
            }
        }

        // Handle movement
        getPieceTracker(movePieceType, colorToMove).movePiece(move.startTile(), move.endTile()); // 0 = white, 1 = black

        if (capturedPieceType == 0 && !isPromotion) {
            try {
                BoardManager.playSound(chessProjectPath + "/reference/sounds/move.wav"); // Play the move sound
            } catch (LineUnavailableException | IOException | UnsupportedAudioFileException exception) {
                exception.printStackTrace(); // Gracefully handle any possible exceptions from the move sound failing
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

        tile[moveTo] = pieceGoingToEndTile; // Update the tile array with the new piece
        tile[moveFrom] = 0; // Remove the moved piece from its old location in the tile array

        // Update whose turn it is to move
        // 0 = white, 1 = black
        whitesMove = !whitesMove;
        colorToMove ^= 1;
        opponentColor ^= 1;
        friendlyColor ^= 1;
        whiteOnBottom = !whiteOnBottom;

        // If a pawn was moved or a piece was captured, reset the 50 move counter
        if (Piece.pieceType(movePiece) == Piece.Pawn) { fiftyMoveRule = 0; }

        if (threeFoldRepetition.containsKey(currentFenPosition)) { // If the current position has already happened, update the number of times it has appeared
            threeFoldRepetition.put(currentFenPosition, threeFoldRepetition.get(currentFenPosition) + 1);
        }
        else { // Otherwise, add it
            threeFoldRepetition.put(currentFenPosition, 1);
        }

        // The move was made and should be played on the board visually
        return !isGhost;
    }
    // end: public void makeMove

}
// end: public class Board