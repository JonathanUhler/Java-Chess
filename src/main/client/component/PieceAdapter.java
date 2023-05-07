package client.component;


import client.Screen;
import engine.util.Coordinate;
import engine.move.Move;
import engine.move.MoveGenerator;
import engine.piece.Piece;
import java.awt.Point;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.List;
import java.util.ArrayList;


/**
 * A mouse adapter that listens on chess pieces. This listener handles the modification of the 
 * {@code highlightedTiles} parameter of its parent {@code PiecePane}. The combination of mouse 
 * listener methods supoprted by this class allow for piece click, drag, and placement.
 */
public class PieceAdapter extends MouseAdapter {

	/** The parent component of this listener. */
	private PiecePane piecePane;
	/** 
	 * The start location of the mouse cursor (used as a temporary register by the 
	 * {@code mouseDragged} method.
	 */
	private Point startLocation;
	/** The amount the cursor has been dragged from the {@code startLocation}. */
	private Point dragOffset;


	/**
	 * Constructs a new {@code PieceAdapter} object.
	 *
	 * @param piecePane  the parent pane component of this listener.
	 */
	public PieceAdapter(PiecePane piecePane) {
		this.piecePane = piecePane;
	}


	/**
	 * Converts a {@code java.awt.Point} object representing a position in pixels to a 
	 * {@code Coordinate} object representing a position in tile-space on the chess board.
	 *
	 * @param p  the point to convert.
	 *
	 * @return a {@code Coordinate} object.
	 *
	 * @throws NullPointerException  if {@code p == null}.
	 */
	private Coordinate pointToCoordinate(Point p) {
		if (p == null)
			throw new NullPointerException("p was null");
		
		int x = p.x;
		int y = p.y;
		
		int xTile = x / Screen.TILE_SIZE;
		int yTile = y / Screen.TILE_SIZE;
		// Swap the coordinate across the x-axis if playing as black, since the perspective would
		// be switched
		yTile = (!this.piecePane.getPlayerColor().equals(Piece.Color.BLACK)) ? (7 - yTile) : yTile;
		xTile = (!this.piecePane.getPlayerColor().equals(Piece.Color.BLACK)) ? xTile : (7 - xTile);
		return new Coordinate(xTile, yTile);
	}


	/**
	 * Invoked when a mouse button has been pressed on a component. Determines and highlights the 
	 * tiles containing the legal end positions of the pressed piece.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		Component pieceClicked = e.getComponent();
		this.piecePane.moveToFront(pieceClicked);
		this.startLocation = pieceClicked.getLocation();
		this.dragOffset = e.getPoint();

		boolean whiteToMove = this.piecePane.getLatestPosition().whiteToMove;
		boolean isWhite = this.piecePane.getPlayerColor().equals(Piece.Color.WHITE);
		Coordinate startTile = this.pointToCoordinate(this.startLocation);

		// If it is this player's turn to move, collect and display all the legal ending tiles
		// for the piece they have selected
		if ((whiteToMove && isWhite) || (!whiteToMove && !isWhite)) {
			List<Coordinate> highlightedTiles = new ArrayList<>();
			List<Move> legalMoves =
				MoveGenerator.generateLegalMoves(this.piecePane.getLatestPosition());

			for (Move m : legalMoves) {
				if (m.getStartTile().equals(startTile))
					highlightedTiles.add(m.getEndTile());
			}
			
			this.piecePane.setHighlightedTiles(highlightedTiles);
		}
	}


	/**
	 * Invoked when a mouse button is pressed on a component and then dragged. MOUSE_DRAGGED 
	 * events will continue  to be delivered to the component where the drag originated until the 
	 * mouse button is released (regardless of whether the mouse position is within the bounds of 
	 * the component).
	 * <p>
	 * As the component is dragged, its position is continuously updated to that of the cursor.
	 */
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


	/**
	 * Invoked when a mouse button has been released on a component. Once released, the start and 
	 * end positions of the move made (as determined by the drag-path of the piece) are sent to the
	 * {@code PiecePane} reference held by this {@code PieceAdapter}.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		Component pieceClicked = e.getComponent();
		Point endLocation = pieceClicked.getLocation();
		// Add 1/2 of a tile to the ending location where the piece was released to position the
		// "release" point over the center of the image of the piece, rather than in the top-left
		// corner
		endLocation.x += Screen.TILE_SIZE / 2;
		endLocation.y += Screen.TILE_SIZE / 2;

		Coordinate startTile = this.pointToCoordinate(this.startLocation);
		Coordinate endTile = this.pointToCoordinate(endLocation);

		this.piecePane.setHighlightedTiles(new ArrayList<>());
	    this.piecePane.adpaterEvent(startTile, endTile);
	}
	
}
