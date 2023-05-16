package variants.bughouse;


import client.Screen;
import engine.util.Coordinate;
import engine.move.Move;
import engine.piece.Piece;
import engine.board.BoardInfo;
import java.awt.Point;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


public class BankAdapter extends MouseAdapter {

	private BugPane bugPane;
	private Piece.Color playerColor;
	private Point startLocation;
	private Point dragOffset;


	public BankAdapter(BugPane bugPane, Piece.Color playerColor) {
		this.bugPane = bugPane;
		this.playerColor = playerColor;
	}


	private Coordinate pointToCoordinate(Point p) {
		if (p == null)
			throw new NullPointerException("p was null");
		
		int x = p.x;
		int y = p.y;
		
		int xTile = x / Screen.TILE_SIZE;
		int yTile = y / Screen.TILE_SIZE;
		// Swap the coordinate across the x-axis if playing as black, since the perspective would
		// be switched
		yTile = (!this.playerColor.equals(Piece.Color.BLACK)) ? (7 - yTile) : yTile;
		xTile = (!this.playerColor.equals(Piece.Color.BLACK)) ? xTile : (7 - xTile);
		return new Coordinate(xTile, yTile);
	}


	@Override
	public void mousePressed(MouseEvent e) {
		Component pieceClicked = e.getComponent();
		this.startLocation = pieceClicked.getLocation();
		this.bugPane.moveToFront(pieceClicked);
		this.dragOffset = e.getPoint();

		int row = (int) ((this.startLocation.y + Screen.TILE_SIZE / 2.0) / Screen.TILE_SIZE);
		Coordinate startTile = new Coordinate(-1, row);
		List<Coordinate> highlightedTiles = new ArrayList<>();

		BoardInfo position = this.bugPane.getPiecePane().getLatestPosition();
		Map<Piece.Type, Integer> myPieces = this.bugPane.getMyPieces();
		List<Move> legalMoves = BugMoveGenerator.generateLegalMoves(position, myPieces);

		for (Move m : legalMoves) {
			if (m.getStartTile().equals(startTile))
				highlightedTiles.add(m.getEndTile());
		}

		this.bugPane.getPiecePane().setHighlightedTiles(highlightedTiles);
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


	@Override
	public void mouseReleased(MouseEvent e) {
		Component pieceClicked = e.getComponent();
		Point endLocation = pieceClicked.getLocation();
		// Add 1/2 of a tile to the ending location where the piece was released to position the
		// "release" point over the center of the image of the piece, rather than in the top-left
		// corner
		endLocation.x += Screen.TILE_SIZE / 2;
		endLocation.y += Screen.TILE_SIZE / 2;

		int row = (int) ((this.startLocation.y + Screen.TILE_SIZE / 2.0) / Screen.TILE_SIZE);
		Coordinate endTile = this.pointToCoordinate(endLocation);

		this.bugPane.getPiecePane().setHighlightedTiles(new ArrayList<>());
	    this.bugPane.adapterEvent(row, endTile);
	}
	
}
