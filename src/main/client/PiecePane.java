// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// PiecePane.java
// Networking-Chess
//
// Create by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package client;


import util.StringUtility;
import util.Coordinate;
import engine.board.BoardInfo;
import engine.piece.Piece;
import engine.move.Move;
import engine.move.MoveGenerator;
import java.io.IOException;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.util.ArrayList;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class PiecePane extends JLayeredPane
//
// A JLayeredPane that holds JLabels for the pieces, making it easier to clear and redraw a position
//
public class PiecePane extends JLayeredPane {

	private GUI gui;
	

	// ----------------------------------------------------------------------------------------------------
	// public PiecePane
	//
	// Arguments--
	//
	//  gui: a GUI object to reference for board information to draw
	//
	public PiecePane(GUI gui) {
		this.gui = gui;
		
		this.setPreferredSize(new Dimension(GUI.TILE_SIZE * 8, GUI.TILE_SIZE * 8));
	}
	// end: public PiecePane


	// ====================================================================================================
	// public void drawPosition
	//
	// Draws a position from the provided GUI object and its information given during construction
	//
	public void drawPosition() {
		// Reset pane
		this.removeAll();

		// Add new pieces
		if (this.gui.getBoardInfo() != null) {
			for (Coordinate c : Coordinate.getAllValidCoordinates()) {
				Piece piece = this.gui.getBoardInfo().getPiece(c);
				if (piece != null) {
					// displayOffsetY used to reflect the board across the x-axis depending on which perspective
					// is being viewed
					int displayOffsetX = (this.gui.getColor() != Piece.Color.BLACK) ? (c.getX()) : (7 - c.getX());
					int displayOffsetY = (this.gui.getColor() != Piece.Color.BLACK) ? (7 - c.getY()) : (c.getY());
					int x = (displayOffsetX * GUI.TILE_SIZE);
					int y = (displayOffsetY * GUI.TILE_SIZE);

					// Get the proper image and draw the piece
					String pieceImageFile = "assets/" + piece.getType() + "" + piece.getColor() + ".png";
					// Read the image file from the JAR location, allowing the jar to be placed anywhere
					ImageIcon pieceIcon = new ImageIcon(Thread.currentThread()
														.getContextClassLoader()
														.getResource(pieceImageFile));
					JLabel pieceLabel = new JLabel(pieceIcon);
					pieceLabel.setBounds(x, y, GUI.TILE_SIZE, GUI.TILE_SIZE);

					// Only allow mouse movement if this player is playing in the game
					if (this.gui.getColor() != Piece.Color.NONE) {
						MouseAdapter mouseAdapter = this.getMouseAdapter();
						pieceLabel.addMouseListener(mouseAdapter);
						pieceLabel.addMouseMotionListener(mouseAdapter);
					}

					this.add(pieceLabel);
				}
			}
		}
	}
	// end: public void drawPosition


	// ====================================================================================================
	// private Coordinate pointToCoordinate
	//
	// Converts a java Point object representing a position in pixels to a Coordinate object representing
	// a tile on the chess board
	//
	// Arguments--
	//
	//  p: the point to convert
	//
	private Coordinate pointToCoordinate(Point p) {
		int x = p.x;
		int y = p.y;
		
		int xTile = x / GUI.TILE_SIZE;
		int yTile = y / GUI.TILE_SIZE;
		// Swap the coordinate across the x-axis if playing as black, since the perspective would
		// be switched
		yTile = (this.gui.getColor() != Piece.Color.BLACK) ? (7 - yTile) : yTile;
		xTile = (this.gui.getColor() != Piece.Color.BLACK) ? (xTile) : (7 - xTile);
		return new Coordinate(xTile, yTile);
	}
	// end: private Coordinate pointToCoordinate
	

	// ====================================================================================================
	// private MouseAdapter getMouseAdapter
	//
	// Returns a MouseAdapter object created with overriden mouse methods to interact with the pieces
	//
	// Returns--
	//
	//  A MouseAdapter object as described above
	//
	private MouseAdapter getMouseAdapter() {
		// Source: https://stackoverflow.com/questions/27915214/how-can-i-drag-images-with-the-mouse-cursor-in-java-gui
		MouseAdapter mouseAdapter = new MouseAdapter() {
				private Point startLocation;
				private Point dragOffset;

				
				@Override
				public void mousePressed(MouseEvent e) {
					Component pieceClicked = e.getComponent();
					PiecePane.this.moveToFront(pieceClicked);
					this.startLocation = pieceClicked.getLocation();
					this.dragOffset = e.getPoint();

					boolean whiteToMove = PiecePane.this.gui.getBoardInfo().whiteToMove;
					boolean isWhite = PiecePane.this.gui.getColor() == Piece.Color.WHITE;
					Coordinate startTile = PiecePane.this.pointToCoordinate(this.startLocation);

					// If it is this player's turn to move, collect and display all the legal ending tiles
					// for the piece they have selected
					if ((whiteToMove && isWhite) || (!whiteToMove && !isWhite)) {
						ArrayList<Coordinate> highlightedTiles = new ArrayList<>();
						ArrayList<Move> legalMoves = MoveGenerator
							.generateLegalMoves(PiecePane.this.gui.getBoardInfo());
						
						for (Move m : legalMoves) {
							if (m.getStartTile().equals(startTile))
								highlightedTiles.add(m.getEndTile());
						}
						PiecePane.this.gui.setHighlightedTiles(highlightedTiles);
					}
				}

				
				@Override
				public void mouseReleased(MouseEvent e) {
					Component pieceClicked = e.getComponent();
					Point endLocation = pieceClicked.getLocation();
					// Add 1/2 of a tile to the ending location where the piece was released to position the
					// "release" point over the center of the image of the piece, rather than in the top-left
					// corner
					endLocation.x += GUI.TILE_SIZE / 2;
					endLocation.y += GUI.TILE_SIZE / 2;
					
					Coordinate startTile = PiecePane.this.pointToCoordinate(this.startLocation);
					Coordinate endTile = PiecePane.this.pointToCoordinate(endLocation);

					PiecePane.this.gui.setHighlightedTiles(new ArrayList<>());
					PiecePane.this.gui.send(startTile, endTile);
				}

				
				@Override
				public void mouseDragged(MouseEvent e) {
					int x = e.getX() - this.dragOffset.x;
					int y = e.getY() - this.dragOffset.y;
					Component pieceClicked = e.getComponent();
					
					Point location = pieceClicked.getLocation();
					location.x += x;
					location.y += y;
					
					pieceClicked.setLocation(location);
				}
			};
		return mouseAdapter;
	}
	// end: private MouseAdapter getMouseAdapter

}
// end: public class PiecePane
