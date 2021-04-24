// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Board.java
// Chess
//
// Created by Jonathan Uhler on 3/25/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import java.awt.*;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Board
//
// Sets up the application frame and creates the 8x8 chessboard
//
public class Board {

    public static final File chessProjectPath = new File("./").getAbsoluteFile().getParentFile().getParentFile(); // Get the path for .../Chess/

    public static final int w = 720, h = w; // Width and height of the JFrame application window
    public static final JFrame appWindow = new JFrame("Chess"); // Create a new application window
    public static final JLayeredPane board = new JLayeredPane(); // Create the layered pane that holds the board and pieces
    public static final JLayeredPane pieces = new JLayeredPane();

    public static final int whiteIndex = 0; // Index for white pieces in arrays (such as PieceTracker arrays below) of white pieces
    public static final int blackIndex = 1; // Index for black pieces in arrays of black pieces
    public static int[] tile = new int[64]; // Every tile on the board

    public static PieceTracker[] pawns; // 1 PieceTracker for all white pawns, 1 PieceTracker for all black pawns
    public static PieceTracker[] knights; // 1 PieceTracker for all white knights, 1 PieceTracker for all black knights
    public static PieceTracker[] bishops; // 1 PieceTracker for all white bishops, 1 PieceTracker for all black bishops
    public static PieceTracker[] rooks; // 1 PieceTracker for all white rooks, 1 PieceTracker for all black rooks
    public static PieceTracker[] queens; // 1 PieceTracker for all white queens, 1 PieceTracker for all black queens
    public static PieceTracker[] kings; // 2 integers. 1 for the tile of the white king, 1 for the tile of the black king
    public static PieceTracker[] allPieceTrackers; // Every piece for both white and black

    static PieceTracker getPieceTracker(int pieceType, int pieceColor) {
        return allPieceTrackers[pieceColor * 8 + pieceType]; // Get a specific piece tracker given only the piece type and color
    }

    public static int fullmoves; // Number of fullmoves played this game
    public static int fiftyMoveRule; // Number of fullmoves since the last pawn movement or piece capture

    public static String currentFenPosition; // Current arrangement of pieces on the board in fen notation
    public static HashMap<String, Integer> threeFoldRepetition = new HashMap<>(); // List of positions and how many times they have appeared in the game

    public static boolean whitesMove; // Is white to move?
    public static int colorToMove; // Which color is to move
    public static int opponentColor; // What is the opposing color
    public static int friendlyColor; // What is the safe color
    public static boolean whiteOnBottom = true; // Did the white player start with pieces on the bottom of the board?

    public static boolean showLegalMoves = true; // Should legal moves be highlighted?

    // Bits 0-3 store white and black kingside/queenside castling legality. 1 = castling allowed, 0 = no castling allowed
    // Bits 4-7 store row of en passant tile (starting at 1, so 0 = no en passant row)
    // Bits 8-13 captured piece
    // Bits 14-... fifty mover counter
    static int currentGameState;

    static int x_pressed = 0; // X position of the mouse when its pressed
    static int y_pressed = 0; // Y position of the mouse when its pressed


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
    public static void loadPosition(String fen) {
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
    // static void initBoard
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
    static void initBoard() {
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
    // end: static void initBoard


    // ====================================================================================================
    // public static void makeMove
    //
    // Makes a move on the board
    //
    // Arguments--
    //
    // move:    the move to be made
    //
    // Returns--
    //
    // None
    //
    public static boolean makeMove(Move move) {
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
            drawPosition(); // Redraw the board
            drawBoard(null); // Redraw the board
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

        // The move was made
        return true;
    }
    // end: public static void makeMove


    // ====================================================================================================
    // public static void drawPosition
    //
    // Draws the pieces onto the board
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // None
    //
    public static void drawPosition() {
        pieces.removeAll();
        pieces.setBounds(0, 0, w, w);

        int pieceStartingX = 2 * (w / 20); // Starting x position for a piece
        int pieceStartingY = 2 * (w / 20); // Starting y position for a piece
        int pieceW = (int) (w * 0.1); // Starting width for a piece
        int pieceH = (int) (w * 0.1); // Starting height for a piece
        ArrayList<Integer> legalMoveTiles = new ArrayList<>(); // List of legal ending moves

        for (PieceTracker pieceTracker : allPieceTrackers) {
            for (int i = 0; i < pieceTracker.pieceCount; i++) {
                String pieceBinaryIdentifier = Integer.toBinaryString(pieceTracker.pieceColor | pieceTracker.pieceType); // Define the binary string for the piece (that is the color | the type)
                JLabel piece = new JLabel(new ImageIcon(chessProjectPath + "/reference/pieces/" + pieceBinaryIdentifier + ".png")); // Create a new label with the correct image
                piece.setBounds(pieceStartingX * ((pieceTracker.tilesWithPieces[i] % 8) + 1), pieceStartingY * (int) ((Math.floor(pieceTracker.tilesWithPieces[i] / 8.0)) + 1), pieceW, pieceH); // Set the size and position of the piece

                int finalI = i;
                // Piece picked up
                piece.addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e) {
                        // Get and store the values of the mouse position when the mouse is pressed
                        x_pressed = e.getX();
                        y_pressed = e.getY();

                        MoveUtility checkMoves = new MoveUtility();
                        List<Short> legalMoves = checkMoves.generateMoves();

                        for (Short legalMove : legalMoves) {
                            int legalStartTile = (legalMove & 0b0000000000111111);
                            int legalEndTile = (legalMove & 0b0000111111000000) >> 6;

                            if (legalStartTile == (int) (Math.round((piece.getLocation().y / 72.0) - 1) * 8 + Math.round((piece.getLocation().x / 72.0) - 1))) {
                                legalMoveTiles.add(legalEndTile);
                            }
                        }

                        drawBoard(legalMoveTiles);
                    }
                });

                // Piece dragged around
                piece.addMouseMotionListener(new MouseMotionAdapter() {
                    @Override public void mouseDragged(MouseEvent e) {
                        // When the mouse is dragged, update the position of the piece image (this doesn't change the location of the piece yet)
                        Point frameRelativeMousePos = BoardManager.frameRelativeMousePosition(appWindow, new Point(e.getXOnScreen(), e.getYOnScreen()));
                        piece.setLocation(frameRelativeMousePos.x - x_pressed, frameRelativeMousePos.y - y_pressed);
                    }
                });

                // Piece placed down
                piece.addMouseListener(new MouseAdapter() {
                    @Override public void mouseReleased(MouseEvent e) {
                        int moveFlag = Move.Flag.none;
                        // If a pawn is on a promotion tile
                        if (Piece.pieceType(tile[pieceTracker.tilesWithPieces[finalI]]) == Piece.Pawn && MoveData.pawnPromotionTiles.contains((int) (Math.round((piece.getLocation().y / 72.0) - 1) * 8 + Math.round((piece.getLocation().x / 72.0) - 1)))) {
                            PromotionUtility promotion = new PromotionUtility();
                            promotion.createPromotionWindow(); // Show a dialog box for promotion

                            moveFlag = promotion.getPromotionPiece(); // Set the promotion flag
                        }

                        // Create a new move and make it on the board
                        Move move = new Move(pieceTracker.tilesWithPieces[finalI], (int) (Math.round((piece.getLocation().y / 72.0) - 1) * 8 + Math.round((piece.getLocation().x / 72.0) - 1)), moveFlag);
                        boolean moveMade = makeMove(move);

                        // Update the board and position
                        if (moveMade) {
                            Settings.drawSettings(); // Update the fen string in the text field
                            loadPosition(FenUtility.changePlayerPerspective(FenUtility.buildFenFromPosition())); // Load the new position
                            drawPosition(); // Draw the position
                            drawBoard(null);
                        }
                    }
                });

                pieces.add(piece, 0); // Add the piece to the pieces layered pane
            }
        }

        appWindow.add(pieces); // Add the board to the frame
        SwingUtilities.updateComponentTreeUI(appWindow); // Reload the JFrame to show any changes
    }
    // end: public static void drawPosition


    // ====================================================================================================
    // public static void drawBoard
    //
    // Draws the board
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // None
    //
    public static void drawBoard(ArrayList<Integer> highlightTiles) {
        if (highlightTiles == null) {
            highlightTiles = new ArrayList<>(); // Handle calls to drawBoard that don't specify legal move tiles to be highlighted
        }

        String currentTheme = ""; // Read the theme
        try { currentTheme = JSONUtility.stringToDictionary(JSONUtility.read(Board.chessProjectPath + "/config/config.json")).get("theme");
        } catch (IOException ioException) { ioException.printStackTrace(); }

        board.removeAll();
        board.setBounds(0, 0, w, w);

        // Create variables for the x/y/w/h dimensions of each tile and the color of each tile
        int tileW = w / 10, tileX, tileY;
        Color c;
        Color lightColor = null, darkColor = null; // Create variables for the light and dark tile color

        // Allow the theme of the board to be changed by changing the colors
        switch (currentTheme) {
            // Classic theme
            case "Gray":
                lightColor = new Color(231, 231, 231);
                darkColor = new Color(88, 88, 88);
                break;
            // Green theme
            case "Green":
                lightColor = new Color(238, 237, 213);
                darkColor = new Color(124, 148, 93);
                break;
            // Blue theme
            case "Blue":
                lightColor = new Color(207, 215, 224);
                darkColor = new Color(110, 143, 167);
                break;
            // Brown theme
            case "Brown":
                lightColor = new Color(236, 217, 185);
                darkColor = new Color(175, 137, 104);
                break;
        }

        // Display the tiles
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                // If the row plus the column is divisible by 2, then set it to one color
                if ((row + col) % 2 == 1) {
                    c = (highlightTiles.contains(((row * 8) + col)) && showLegalMoves) ? new Color(161, 86, 86) : darkColor;
                }
                // Otherwise, set it to the other color
                else {
                    c = (highlightTiles.contains(((row * 8) + col)) && showLegalMoves) ? new Color(238, 156, 156) : lightColor;
                }

                // Update the x/y position of each tile
                tileX = (col + 1) * tileW;
                tileY = (row + 1) * tileW;

                JLabel newTile = new JLabel(); // Create a new tile object to add to the layered pane
                newTile.setBorder(BorderFactory.createLineBorder(c, w / 2)); // Set the color of the tile
                newTile.setBounds(tileX, tileY, tileW, tileW); // Set the size and location of the tile

                board.add(newTile, 1); // Add the tile below the pieces
            }
        }

        // Add a black border around the board
        JLabel boardBorder = new JLabel();
        boardBorder.setBorder(BorderFactory.createLineBorder(Color.black, tileW / 16));
        boardBorder.setBounds(72 - (tileW / 16), 72 - (tileW / 16), tileW * 8 + (tileW / 16), tileW * 8 + (tileW / 16));
        board.add(boardBorder, 0);

        appWindow.add(board); // Add the tiles to the board
        SwingUtilities.updateComponentTreeUI(appWindow); // Reload the JFrame to show any changes
    }
    // end: public static void drawBoard


    // ====================================================================================================
    // public static void createApplication
    //
    // Creates the JFrame
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // None
    //
    public static void createApplication() {
        appWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Terminate the frame when the 'x' button of the application window is pressed
        appWindow.setBounds(20, 30, w, h); // Set the size of the frame
        appWindow.setLayout(null);
        appWindow.setResizable(false); // Prevent the JFrame from being resized
        appWindow.setVisible(true); // Show the frame
    }
    // end: public static void createApplication

}
// end: public class Board