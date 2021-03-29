// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Board.java
// Chess
//
// Created by Jonathan Uhler on 3/25/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;
import javax.swing.*;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Board
//
// Sets up the application frame and creates the 8x8 chessboard
//
public class Board {

    public static int w = 720, h = w; // Width and height of the JFrame application window
    public static JFrame appWindow = new JFrame("Chess"); // Create a new application window
    public static JLayeredPane board = new JLayeredPane(); // Create the layered pane that holds the board and pieces

    public static final int whiteIndex = 0; // Index for white pieces in arrays (such as PieceTracker arrays below) of white pieces
    public static final int blackIndex = 1; // Index for black pieces in arrays of black pieces
    public static int[] tile = new int[64]; // Every tile on the board

    public static PieceTracker[] pawns; // 1 PieceTracker for all white pawns, 1 PieceTracker for all black pawns
    public static PieceTracker[] knights; // 1 PieceTracker for all white knights, 1 PieceTracker for all black knights
    public static PieceTracker[] bishops; // 1 PieceTracker for all white bishops, 1 PieceTracker for all black bishops
    public static PieceTracker[] rooks; // 1 PieceTracker for all white rooks, 1 PieceTracker for all black rooks
    public static PieceTracker[] queens; // 1 PieceTracker for all white queens, 1 PieceTracker for all black queens
    public static int[] kings; // 2 integers. 1 for the tile of the white king, 1 for the tile of the black king
    public static PieceTracker[] allPieceTrackers; // Every piece for both white and black

    public static int plies; // Number of halfmoves played this game
    public static int fiftyMoveRule; // Number of moves since the last pawn movement or piece capture

    public static boolean whitesMove; // Is white to move?
    public static int colorToMove; // Which color is to move
    public static int opponentColor; // What is the opposing color

    // Bits 0-3 store white and black kingside/queenside castling legality. 1 = castling allowed, 0 = no castling allowed
    // Bits 4-7 store row of en passant tile (starting at 1, so 0 = no en passant row)
    // Bits 8-13 captured piece
    // Bits 14-... fifty mover counter
    public static Stack<Integer> gameStateHistory;
    public static int currentGameState;


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
        initBoard();
        // Get the FEN info for the position being loaded
        FenInfo loadedPosition = FenUtility.loadPositionFromFen(fen);

        // Loop over every tile of the board
        for (int tileIndex = 0; tileIndex < 64; tileIndex++) {
            int piece = loadedPosition.tiles[tileIndex]; // Get the piece on the current tile (note this might be type Piece.None, this is handled later)
            tile[tileIndex] = piece; // Save the current piece to a global array of the tiles on the board

            // Make sure the current tile has a piece
            if (piece != Piece.None) {
                int pieceType = Piece.pieceType(piece);
                int pieceColorIndex = (Piece.findColor(piece, Piece.White)) ? whiteIndex : blackIndex;

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
                    kings[pieceColorIndex] = tileIndex;
                }
            }
        }

        whitesMove = loadedPosition.whiteToMove; // Figure out if it is white's turn to move
        colorToMove = (whitesMove) ? Piece.White : Piece.Black; // If it is white's move, then the color to move is white, otherwise it must be black
        opponentColor = (whitesMove) ? 0 : 1;

        // Create game state
        // Castling priorities
        int whiteCastle = ((loadedPosition.whiteCastleKingside) ? 1 << 0 : 0) | ((loadedPosition.whiteCastleQueenside) ? 1 << 1 : 0);
        int blackCastle = ((loadedPosition.blackCastleKingside) ? 1 << 2 : 0) | ((loadedPosition.blackCastleQueenside) ? 1 << 3 : 0);
        // En passant availability
        int epState = loadedPosition.enPassantRow << 4;
        // Current game state
        short initialGameState = (short) (whiteCastle | blackCastle | epState);
        gameStateHistory.push((int) initialGameState); // Add the game state to an archive for threefold repetition
        currentGameState = initialGameState; // Update the current game state
        plies = loadedPosition.plies; // Update the number of plies
    }
    // end: public void loadPosition


    // ====================================================================================================
    // static void initBoard
    //
    // Initializes information about the state of the board and the pieces (including PieceTrackers)
    //
    // Areguments--
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
        kings = new int[2];
        plies = 0;
        fiftyMoveRule = 0;
        gameStateHistory = new Stack<>();

        // Create the lists of each piece  |  Pieces for white                      |  Pieces for black
        knights = new PieceTracker[]    { new PieceTracker(10), new PieceTracker(10) };
        pawns = new PieceTracker[]      { new PieceTracker(8),  new PieceTracker(8)  };
        rooks = new PieceTracker[]      { new PieceTracker(10), new PieceTracker(10) };
        bishops = new PieceTracker[]    { new PieceTracker(10), new PieceTracker(10) };
        queens = new PieceTracker[]     { new PieceTracker(9),  new PieceTracker(9)  };

        PieceTracker emptyList =          new PieceTracker(0);

        // Every piece on the board
        allPieceTrackers = new PieceTracker[] {
                // White
                pawns[whiteIndex],
                knights[whiteIndex],
                bishops[whiteIndex],
                rooks[whiteIndex],
                queens[whiteIndex],
                // Black
                pawns[blackIndex],
                knights[blackIndex],
                bishops[blackIndex],
                rooks[blackIndex],
                queens[blackIndex],
        };
    }
    // end: static void initBoard


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

        //boardState.setLayout(null); // Set the layout to none
        board.setLayout(null);
        int pieceStartingX = 2 * (w / 20); // Starting x position for a piece
        int pieceStartingY = 2 * (w / 20); // Starting y position for a piece
        int pieceW = (int) (w * 0.1); // Starting width for a piece
        int pieceH = (int) (w * 0.1); // Starting height for a piece

        for (int i = 0; i < 64; i++) {
            // White pawns
            if (i < pawns[whiteIndex].pieceCount) {
                JLabel whitePawn = new JLabel(new ImageIcon("./Pieces/01001.png")); // Add the piece to a label
                whitePawn.setBounds(pieceStartingX * ((pawns[whiteIndex].tilesWithPieces[i] % 8) + 1), pieceStartingY * (int) ((Math.floor(pawns[whiteIndex].tilesWithPieces[i] / 8)) + 1), pieceW, pieceH); // Position the piece
                board.add(whitePawn, 0); // add each piece to the board
            }
            // Black pawns
            if (i < pawns[blackIndex].pieceCount) {
                JLabel blackPawn = new JLabel(new ImageIcon("./Pieces/10001.png")); // Add the piece to a label
                blackPawn.setBounds(pieceStartingX * ((pawns[blackIndex].tilesWithPieces[i] % 8) + 1),pieceStartingY * (int) ((Math.floor(pawns[blackIndex].tilesWithPieces[i] / 8)) + 1), pieceW, pieceH); // Position the piece
                board.add(blackPawn, 0); // add each piece to the board
            }
            // White knights
            if (i < knights[whiteIndex].pieceCount) {
                JLabel whiteKnight = new JLabel(new ImageIcon("./Pieces/01010.png")); // Add the piece to a label
                whiteKnight.setBounds(pieceStartingX * ((knights[whiteIndex].tilesWithPieces[i] % 8) + 1), pieceStartingY * (int) ((Math.floor(knights[whiteIndex].tilesWithPieces[i] / 8)) + 1), pieceW, pieceH); // Position the piece
                board.add(whiteKnight, 0); // add each piece to the board
            }
            // Black knights
            if (i < knights[blackIndex].pieceCount) {
                JLabel blackKnight = new JLabel(new ImageIcon("./Pieces/10010.png")); // Add the piece to a label
                blackKnight.setBounds(pieceStartingX * ((knights[blackIndex].tilesWithPieces[i] % 8) + 1), pieceStartingY * (int) ((Math.floor(knights[blackIndex].tilesWithPieces[i] / 8)) + 1), pieceW, pieceH); // Position the piece
                board.add(blackKnight, 0); // add each piece to the board
            }
            // White bishops
            if (i < bishops[whiteIndex].pieceCount) {
                JLabel whiteBishop = new JLabel(new ImageIcon("./Pieces/01011.png")); // Add the piece to a label
                whiteBishop.setBounds(pieceStartingX * ((bishops[whiteIndex].tilesWithPieces[i] % 8) + 1), pieceStartingY * (int) ((Math.floor(bishops[whiteIndex].tilesWithPieces[i] / 8)) + 1), pieceW, pieceH); // Position the piece
                board.add(whiteBishop, 0); // add each piece to the board
            }
            // Black bishops
            if (i < bishops[blackIndex].pieceCount) {
                JLabel blackBishop = new JLabel(new ImageIcon("./Pieces/10011.png")); // Add the piece to a label
                blackBishop.setBounds(pieceStartingX * ((bishops[blackIndex].tilesWithPieces[i] % 8) + 1), pieceStartingY * (int) ((Math.floor(bishops[blackIndex].tilesWithPieces[i] / 8)) + 1), pieceW, pieceH); // Position the piece
                board.add(blackBishop, 0); // add each piece to the board
            }
            // White rooks
            if (i < rooks[whiteIndex].pieceCount) {
                JLabel whiteRook = new JLabel(new ImageIcon("./Pieces/01100.png")); // Add the piece to a label
                whiteRook.setBounds(pieceStartingX * ((rooks[whiteIndex].tilesWithPieces[i] % 8) + 1), pieceStartingY * (int) ((Math.floor(rooks[whiteIndex].tilesWithPieces[i] / 8)) + 1), pieceW, pieceH); // Position the piece
                board.add(whiteRook, 0); // add each piece to the board
            }
            // Black rooks
            if (i < rooks[blackIndex].pieceCount) {
                JLabel blackRook = new JLabel(new ImageIcon("./Pieces/10100.png")); // Add the piece to a label
                blackRook.setBounds(pieceStartingX * ((rooks[blackIndex].tilesWithPieces[i] % 8) + 1), pieceStartingY * (int) ((Math.floor(rooks[blackIndex].tilesWithPieces[i] / 8)) + 1), pieceW, pieceH); // Position the piece
                board.add(blackRook, 0); // add each piece to the board
            }
            // White queens
            if (i < queens[whiteIndex].pieceCount) {
                JLabel whiteQueen = new JLabel(new ImageIcon("./Pieces/01110.png")); // Add the piece to a label
                whiteQueen.setBounds(pieceStartingX * ((queens[whiteIndex].tilesWithPieces[i] % 8) + 1), pieceStartingY * (int) ((Math.floor(queens[whiteIndex].tilesWithPieces[i] / 8)) + 1), pieceW, pieceH); // Position the piece
                board.add(whiteQueen, 0); // add each piece to the board
            }
            // Black queens
            if (i < queens[blackIndex].pieceCount) {
                JLabel blackQueen = new JLabel(new ImageIcon("./Pieces/10110.png")); // Add the piece to a label
                blackQueen.setBounds(pieceStartingX * ((queens[blackIndex].tilesWithPieces[i] % 8) + 1), pieceStartingY * (int) ((Math.floor(queens[blackIndex].tilesWithPieces[i] / 8)) + 1), pieceW, pieceH); // Position the piece
                board.add(blackQueen, 0); // add each piece to the board
            }
            // White and Black king
            if (i < kings.length) {
                JLabel whiteKing = new JLabel(new ImageIcon("./Pieces/01111.png")); // Add the piece to a label
                whiteKing.setBounds(pieceStartingX * ((kings[whiteIndex] % 8) + 1), pieceStartingY * (int) ((Math.floor(kings[whiteIndex] / 8)) + 1), pieceW, pieceH); // Position the piece
                JLabel blackKing = new JLabel(new ImageIcon("./Pieces/10111.png")); // Add the piece to a label
                blackKing.setBounds(pieceStartingX * ((kings[blackIndex] % 8) + 1), pieceStartingY * (int) ((Math.floor(kings[blackIndex] / 8)) + 1), pieceW, pieceH); // Position the piece
                board.add(whiteKing, 0); // Add the white king to the board
                board.add(blackKing, 0); // Add the black king to the board
            }
        }

        appWindow.add(board); // Add the board to the frame
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
    public static void drawBoard(String theme) {

        // Create variables for the x/y/w/h dimensions of each tile and the color of each tile
        int w = 72, h = w, x, y;
        Color c;
        Color lightColor = null, darkColor = null; // Create variables for the light and dark tile color

        JComboBox<String> boardThemesDropdown = new JComboBox<String>(new String[] {"Classic", "Green", "Blue", "Brown"}); // Create a dropdown menu for board color themes
        // Create an action listener for the theme dropdown menu
        ActionListener boardThemeChanged  = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                // MARK: here, the new user's theme must be saved in a config file
                drawBoard((String) boardThemesDropdown.getSelectedItem()); // When the user changes the theme, update the board
                drawPosition(); // Update the position as well
            }
        };
        boardThemesDropdown.addActionListener(boardThemeChanged); // Add the action listener to the dropdown menu
        boardThemesDropdown.setBounds(w, 0, w * 2, h); // Set the location of the dropdown menu
        board.add(boardThemesDropdown); // Add the dropdown menu to the view

        // Allow the theme of the board to be changed by changing the colors
        // Classic theme
        if (theme == "Classic") {
            lightColor = new Color(231, 231, 231);
            darkColor = new Color(54, 54, 54);
        }
        // Green theme
        else if (theme == "Green") {
            lightColor = new Color(213, 224, 207);
            darkColor = new Color(82, 137, 67);
        }
        // Blue theme
        else if (theme == "Blue") {
            lightColor = new Color(207, 215, 224);
            darkColor = new Color(67, 109, 137);
        }
        // Brown theme
        else if (theme == "Brown") {
            lightColor = new Color(220, 204, 194);
            darkColor = new Color(127, 86, 64);
        }

        // Display the tiles
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                // If the row plus the column is divisible by 2, then set it to one color
                if ((row + col) % 2 == 1) {
                    c = darkColor;
                }
                // Otherwise, set it to the other color
                else {
                    c = lightColor;
                }

                // Update the x/y position of each tile
                x = (col + 1) * w;
                y = (row + 1) * h;

                JLabel newTile = new JLabel(); // Create a new tile object to add to the layered pane
                newTile.setBorder(BorderFactory.createLineBorder(c, w / 2)); // Set the color of the tile
                newTile.setBounds(x, y, w, h); // Set the size and location of the tile
                board.add(newTile, 2); // Add the tile below the pieces
            }
        }

        // Add a black border around the board
        JLabel boardBorder = new JLabel();
        boardBorder.setBorder(BorderFactory.createLineBorder(Color.black, w / 16));
        boardBorder.setBounds(72, 72, w * 8, h * 8);
        board.add(boardBorder, 1);

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
        appWindow.setBounds(30, 30, w, h); // Set the size of the frame
        appWindow.setResizable(false); // Prevent the JFrame from being resized
        appWindow.setVisible(true); // Show the frame
    }
    // end: public static void createApplication
}
// end: public class Board