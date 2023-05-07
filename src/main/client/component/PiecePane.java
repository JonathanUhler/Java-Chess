package client.component;


import client.Screen;
import server.Communication;
import engine.util.Coordinate;
import engine.board.BoardInfo;
import engine.piece.Piece;
import engine.move.Move;
import engine.move.MoveGenerator;
import java.io.IOException;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


/**
 * A pane that holds chess pieces as {@code JLabel} objects. This component handles mouse control 
 * of the game and drawing of the chess pieces and board. A secondary graphical class is expected 
 * to act as a screen which interfaces with a chess server.
 * <p>
 * Action listeners can be added to this class, which will be notified with an {@code ActionEvent}
 * object upon user interaction with the {@code PieceAdapter} managed by this class. In this
 * case, an "action" is always generated when the {@code PieceAdapter.mouseReleased} method
 * is invoked (e.g. when the end-user lets go of the piece being dragged).
 * <p>
 * The events sent to action listeners have the following behavior:
 * <ul>
 * <li> A non-{@code null} {@code ActionEvent} is sent to the {@code actionPerformed} method
 *      of all registered listeners
 * <li> The {@code source} component of the event is always {@code this} piece pane
 * <li> The {@code id} of the event is always {@code ACTION_PERFORMED}
 * <li> The {@code command} string of the event is a serialized {@code Map} generated using
 *      the {@code Communication.cmdMove} method (this payload contains the move's start tile,
 *      end tile, and flag)
 * </ul>
 * The move data from the action event can be retrieved by the following:
 * <pre>
 * {@code
 * String commandStr = e.getActionCommand();
 * Map<String, String> command = Communication.deserialize(commandStr);
 * Coordinate startTile = Coordinate.fromString(command.get(Communication.KEY_START));
 * Coordinate endTile = Coordinate.fromString(command.get(Communication.KEY_END));
 * Move.Flag flag = Move.Flag.valueOf(command.get(Communication.KEY_FLAG));
 * Move move = new Move(startTile, endTile, flag);
 * }
 * </pre>
 *
 * @see server.Communication
 * @see java.awt.event.ActionEvent
 *
 * @author Jonathan Uhler
 */
public class PiecePane extends JLayeredPane {

	/** List of all components listening for adapter events fromt this pane. */
	private List<ActionListener> actionListeners;
	

	/** List of tiles that should be highlighted, used to display legal moves. */
	private List<Coordinate> highlightedTiles;
	/** The color of the current player, used to change the perspective of the board. */
	private Piece.Color playerColor;
	/** The latest position {@code drawPosition} was called with. */
	private BoardInfo latestPosition;
	

	/**
	 * Constructs a {@code PiecePane} object.
	 *
	 * @param playerColor  the color of the player viewing this {@code PiecePane}.
	 *
	 * @throws NullPointerException  if {@code playerColor == null}.
	 */
	public PiecePane(Piece.Color playerColor) {
		if (playerColor == null)
			throw new NullPointerException("playerColor was null");

		this.actionListeners = new ArrayList<>();

		this.highlightedTiles = new ArrayList<>();
		this.playerColor = playerColor;
		this.latestPosition = null;
		this.setPreferredSize(new Dimension(Screen.TILE_SIZE * 8, Screen.TILE_SIZE * 8));
	}


	/**
	 * Returns the player color of this {@code PiecePane}. This determines which
	 * perspective the pane displays.
	 *
	 * @return the player color of this {@code PiecePane}.
	 */
	public Piece.Color getPlayerColor() {
		return this.playerColor;
	}


	/**
	 * Returns the latest {@code BoardInfo} object seen by this {@code PiecePane}.
	 *
	 * @return the latest {@code BoardInfo} object seen by this {@code PiecePane}.
	 */
	public BoardInfo getLatestPosition() {
		return this.latestPosition;
	}


	/**
	 * Sets the list of hightlighted tiles that are displayed by this {@code PiecePane}. Any tile
	 * included in the argument list that is not a valid tile 
	 * ({@code highlightedTiles.get(i).isValidTile() == false}) is ignored, but no error is thrown.
	 *
	 * @param highlightedTiles  the list of tiles to highlight.
	 *
	 * @throws NullPointerException  if {@code highlightedTiles == null}.
	 */
	public void setHighlightedTiles(List<Coordinate> highlightedTiles) {
		if (highlightedTiles == null)
			throw new NullPointerException("highlightedTiles was null");
		this.highlightedTiles = highlightedTiles;
		this.repaint();
	}


	/**
	 * Paints the chess board below the pieces on this {@code PiecePane}.
	 *
	 * @param g  the {@code Graphics} object to paint on.
	 */
	@Override
	public void paintComponent(Graphics g) {
		// Draw the chess board tiles
		boolean isWhite = this.playerColor.equals(Piece.Color.WHITE);
		for (Coordinate c : Coordinate.getAllValidCoordinates()) {
			if ((c.getX() + c.getY()) % 2 == 0) {
				// Dark tiles
				if (this.highlightedTiles.contains(c))
					g.setColor(new Color(161, 86, 86));
				else
					g.setColor(new Color(175, 137, 104));
			}
			else {
				// Light tiles
				if (this.highlightedTiles.contains(c))
					g.setColor(new Color(238, 156, 156));
				else
					g.setColor(new Color(236, 217, 185));
			}

			int displayOffsetX = isWhite ? c.getX() : (7 - c.getX());
			int displayOffsetY = isWhite ? (7 - c.getY()) : c.getY();
			g.fillRect(displayOffsetX * Screen.TILE_SIZE,
					   displayOffsetY * Screen.TILE_SIZE,
					   Screen.TILE_SIZE, Screen.TILE_SIZE);
		}
	}


	/**
	 * Draws the pieces of a given position. This method is responsible for updating 
	 * {@code latestPosition}. A call to {@code PiecePane::getLatestPosition} after a call to 
	 * {@code PiecePane::drawPosition} will return the argument passed into this method.
	 *
	 * @param position  the position to draw.
	 *
	 * @throws NullPointerException  if {@code position == null}.
	 */
	public void drawPosition(BoardInfo position) {
		if (position == null)
			throw new NullPointerException("position was null");
		this.latestPosition = position;
		
		// Reset pane
		this.removeAll();

		// Add new pieces
		boolean isWhite = this.playerColor.equals(Piece.Color.WHITE);
		for (Coordinate c : Coordinate.getAllValidCoordinates()) {
			// dispOffset used to reflect the board across the x-axis depending on the perspective
			int dispOffsetX = isWhite ? c.getX() : (7 - c.getX());
			int dispOffsetY = isWhite ? (7 - c.getY()) : c.getY();
			int x = (dispOffsetX * Screen.TILE_SIZE);
			int y = (dispOffsetY * Screen.TILE_SIZE);
			
			// Manage pieces
			Piece piece = position.getPiece(c);
			if (piece == null)
				continue;
			
			// Get the proper image and draw the piece
			String pieceImageFile = "assets/images/" + piece.getType() + piece.getColor() + ".png";
			// Read the image file from the JAR location, allowing the jar to be placed anywhere
			ImageIcon pieceIcon = new ImageIcon(Thread.currentThread()
												.getContextClassLoader()
												.getResource(pieceImageFile));
			JLabel pieceLabel = new JLabel(pieceIcon);
			pieceLabel.setBounds(x, y, Screen.TILE_SIZE, Screen.TILE_SIZE);
			
			// Only allow mouse movement if this player is playing in the game
			if (!this.playerColor.equals(Piece.Color.NONE)) {
				MouseAdapter mouseAdapter = new PieceAdapter(this);
				pieceLabel.addMouseListener(mouseAdapter);
				pieceLabel.addMouseMotionListener(mouseAdapter);
			}

			// Add the piece
			this.add(pieceLabel);
		}
	}

	/**
	 * Adds the specified action listener to receive action events from this pane.
	 *
	 * @param l  the action listener to be added.
	 */
	public void addActionListener(ActionListener l) {
		this.actionListeners.add(l);
	}


	/**
	 * Removes the specified action listener so that it no longer receives action events from 
	 * this pane.
	 *
	 * @param l  the action listener to be removed.
	 */
	public void removeActionListener(ActionListener l) {
		this.actionListeners.remove(l);
	}


	/**
	 * Invoked when an event occurs in this pane's {@code PieceAdapter}. The arguments
	 * given represent the start and end tile of a piece that was moved by the adapter. These
	 * tiles may or may not represent a legal move. Any argument that is {@code null}
	 * or not a valid tile results in the call to this method being ignored.
	 *
	 * @param startTile  the origin of the piece moved.
	 * @param endTile    the destination of the piece moved.
	 */
	public void adpaterEvent(Coordinate startTile, Coordinate endTile) {
		if (startTile == null || endTile == null)
			return;
		if (!startTile.isValidTile() || !endTile.isValidTile())
			return;

		// Get all components of the move
		Piece piece = this.latestPosition.getPiece(startTile);
		Coordinate enPassantTile = this.latestPosition.enPassantTile;
		Move.Flag flag = Move.inferFlag(piece, startTile, endTile, enPassantTile);

		// Generate a stringified payload to send with the action event
		Move move = new Move(startTile, endTile, flag);
		Map<String, String> command = Communication.cmdMove(move);
		String commandStr = Communication.serialize(command);

	    // Notify all listeners of this pane that an adapter action was performed
		ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, commandStr);
		for (ActionListener l : this.actionListeners)
			l.actionPerformed(actionEvent);
	}

}
