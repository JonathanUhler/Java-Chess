// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Graphics.java
// Chess
//
// Created by Jonathan Uhler on 4/25/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Graphics
//
// Draws graphical elements onto the screen
//
public class Graphics {

    public final int w = 720, h = w; // Width and height of the JFrame application window
    public final JFrame appWindow = new JFrame("Chess"); // Create a new application window
    public final JLayeredPane board = new JLayeredPane(); // Create the layered pane that holds the board and pieces
    public final JLayeredPane pieces = new JLayeredPane();

    int x_pressed = 0; // X position of the mouse when its pressed
    int y_pressed = 0; // Y position of the mouse when its pressed


    // ====================================================================================================
    // public void drawPosition
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
    public void drawPosition() {
        pieces.removeAll();
        pieces.setBounds(0, 0, w, w);

        int pieceStartingX = 2 * (w / 20); // Starting x position for a piece
        int pieceStartingY = 2 * (w / 20); // Starting y position for a piece
        int pieceW = (int) (w * 0.1); // Starting width for a piece
        int pieceH = (int) (w * 0.1); // Starting height for a piece
        ArrayList<Integer> legalMoveTiles = new ArrayList<>(); // List of legal ending moves

        for (PieceTracker pieceTracker : Chess.board.allPieceTrackers) {
            for (int i = 0; i < pieceTracker.pieceCount; i++) {
                String pieceBinaryIdentifier = Integer.toBinaryString(pieceTracker.pieceColor | pieceTracker.pieceType); // Define the binary string for the piece (that is the color | the type)
                JLabel piece = new JLabel(new ImageIcon(Chess.board.chessProjectPath + "/reference/pieces/" + pieceBinaryIdentifier + ".png")); // Create a new label with the correct image
                piece.setBounds(pieceStartingX * ((pieceTracker.tilesWithPieces[i] % 8) + 1), pieceStartingY * (int) ((Math.floor(pieceTracker.tilesWithPieces[i] / 8.0)) + 1), pieceW, pieceH); // Set the size and position of the piece
                System.out.println(Arrays.toString(pieceTracker.tilesWithPieces));

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

                        // Figure out move flag
                        // En passant and promotion
                        if (Piece.pieceType(Chess.board.tile[pieceTracker.tilesWithPieces[finalI]]) == Piece.Pawn) {
                            // Promotion
                            if (MoveData.pawnPromotionTiles.contains((int) (Math.round((piece.getLocation().y / 72.0) - 1) * 8 + Math.round((piece.getLocation().x / 72.0) - 1)))) {
                                PromotionUtility promotion = new PromotionUtility();
                                promotion.createPromotionWindow(); // Show a dialog box for promotion

                                moveFlag = promotion.getPromotionPiece(); // Set the promotion flag
                            }

                            // Pawn pushed two
                            if (((int) (Math.round((piece.getLocation().y / 72.0) - 1) * 8 + Math.round((piece.getLocation().x / 72.0) - 1)) + 16) == pieceTracker.tilesWithPieces[finalI]) {
                                moveFlag = Move.Flag.pawnTwoForward;
                            }

                            // En passant
                            int enPassantCol = FenUtility.loadPositionFromFen(FenUtility.buildFenFromPosition()).enPassantCol;
                            int enPassantTile = (enPassantCol != -1) ? (2 * 8) + enPassantCol : -1; // Tile behind pawn that moved 2
                            if ((int) (Math.round((piece.getLocation().y / 72.0) - 1) * 8 + Math.round((piece.getLocation().x / 72.0) - 1)) == enPassantTile) {
                                moveFlag = Move.Flag.enPassantCapture;
                            }
                        }

                        // Create a new move and make it on the board
                        Move move = new Move(pieceTracker.tilesWithPieces[finalI], (int) (Math.round((piece.getLocation().y / 72.0) - 1) * 8 + Math.round((piece.getLocation().x / 72.0) - 1)), moveFlag);
                        boolean moveMade = Chess.board.makeMove(move, false);

                        // Update the board and position
                        if (moveMade) {
                            Settings.drawSettings(); // Update the fen string in the text field
                            Chess.board.loadPosition(FenUtility.changePlayerPerspective(FenUtility.buildFenFromPosition())); // Load the new position
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
    // end: public void drawPosition


    // ====================================================================================================
    // public void drawBoard
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
    public void drawBoard(ArrayList<Integer> highlightTiles) {
        if (highlightTiles == null) {
            highlightTiles = new ArrayList<>(); // Handle calls to drawBoard that don't specify legal move tiles to be highlighted
        }

        String currentTheme = ""; // Read the theme
        try { currentTheme = JSONUtility.stringToDictionary(JSONUtility.read(Chess.board.chessProjectPath + "/config/config.json")).get("theme");
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
                    c = (highlightTiles.contains(((row * 8) + col)) && Chess.board.showLegalMoves) ? new Color(161, 86, 86) : darkColor;
                }
                // Otherwise, set it to the other color
                else {
                    c = (highlightTiles.contains(((row * 8) + col)) && Chess.board.showLegalMoves) ? new Color(238, 156, 156) : lightColor;
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
    // end: public void drawBoard


    // ====================================================================================================
    // public void createApplication
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
    public void createApplication() {
        appWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Terminate the frame when the 'x' button of the application window is pressed
        appWindow.setBounds(20, 30, w, h); // Set the size of the frame
        appWindow.setLayout(null);
        appWindow.setResizable(false); // Prevent the JFrame from being resized
        appWindow.setVisible(true); // Show the frame
    }
    // end: public void createApplication

}
// end: public class Graphics
