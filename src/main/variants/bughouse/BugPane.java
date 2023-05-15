package variants.bughouse;


import client.Screen;
import client.component.PiecePane;
import client.component.PieceAdapter;
import server.Communication;
import engine.piece.Piece;
import engine.util.Coordinate;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


public class BugPane extends JLayeredPane {

	/** List of all components listening for adapter events fromt this pane. */
	private List<ActionListener> actionListeners;

	/** The color of the current player, used to change the perspective of the board. */
	private Piece.Color playerColor;
	/** The piece pane managed by this crazyhouse pane. */
	private PiecePane piecePane;


	public BugPane(Piece.Color playerColor, PiecePane piecePane) {
	    if (playerColor == null)
			throw new NullPointerException("playerColor was null");
		if (piecePane == null)
			throw new NullPointerException("piecePane was null");

		this.actionListeners = new ArrayList<>();

		this.playerColor = playerColor;
		this.piecePane = piecePane;

		this.setPreferredSize(new Dimension(Screen.TILE_SIZE * 10, Screen.TILE_SIZE * 8));

		this.piecePane.setBounds(0, 0, Screen.TILE_SIZE * 8, Screen.TILE_SIZE * 8);
		this.drawPiecePane();
	}


    /**
	 * Returns the player color of this {@code BugPane}. This determines which
	 * perspective the pane displays.
	 *
	 * @return the player color of this {@code BugPane}.
	 */
	public Piece.Color getPlayerColor() {
		return this.playerColor;
	}


	public PiecePane getPiecePane() {
		return this.piecePane;
	}


	/**
	 * Paints this pane.
	 *
	 * @param g  the {@code Graphics} object to paint on.
	 */
	@Override
	public void paintComponent(Graphics g) {
		this.piecePane.paintComponent(g);
	}


	public void drawPiecePane() {
		this.add(this.piecePane);
	}


	public void drawBank(int myPawns, int myKnights, int myBishops, int myRooks, int myQueens,
						 int opPawns, int opKnights, int opBishops, int opRooks, int opQueens)
	{
		// Reset pane
		this.removeAll();
		this.drawPiecePane();

		// Draw player's pieces in the bank
		this.drawPieceInBank(new Piece(Piece.Type.PAWN, this.playerColor), myPawns, 3);
		this.drawPieceInBank(new Piece(Piece.Type.KNIGHT, this.playerColor), myKnights, 4);
		this.drawPieceInBank(new Piece(Piece.Type.BISHOP, this.playerColor), myBishops, 5);
		this.drawPieceInBank(new Piece(Piece.Type.ROOK, this.playerColor), myRooks, 6);
		this.drawPieceInBank(new Piece(Piece.Type.QUEEN, this.playerColor), myQueens, 7);

		// Draw opponent's pieces in their bank (so this player can see what they have)
		boolean isWhite = this.playerColor.equals(Piece.Color.WHITE);
		Piece.Color opponentColor = isWhite ? Piece.Color.BLACK : Piece.Color.WHITE;
		this.drawPieceInBank(new Piece(Piece.Type.PAWN, opponentColor), opPawns, 4);
		this.drawPieceInBank(new Piece(Piece.Type.KNIGHT, opponentColor), opKnights, 3);
		this.drawPieceInBank(new Piece(Piece.Type.BISHOP, opponentColor), opBishops, 2);
		this.drawPieceInBank(new Piece(Piece.Type.ROOK, opponentColor), opRooks, 1);
		this.drawPieceInBank(new Piece(Piece.Type.QUEEN, opponentColor), opQueens, 0);
	}


	private void drawPieceInBank(Piece piece, int count, int row) {
		if (count <= 0)
			return;

		boolean isMine = piece.getColor().equals(this.playerColor);

		// Get the proper image and draw the piece
		String pieceImageFile = "assets/images/" + piece.getType() + piece.getColor() + ".png";
		// Read the image file from the JAR location, allowing the jar to be placed anywhere
		ImageIcon pieceIcon = new ImageIcon(Thread.currentThread()
											.getContextClassLoader()
											.getResource(pieceImageFile));
		JLabel pieceLabel = new JLabel(pieceIcon);

		if (isMine) {
			MouseAdapter mouseAdapter = new BankAdapter(this, this.piecePane.getPlayerColor());
			pieceLabel.addMouseListener(mouseAdapter);
			pieceLabel.addMouseMotionListener(mouseAdapter);
		}

		int col = isMine ? Screen.TILE_SIZE * 8 : Screen.TILE_SIZE * 9;
		pieceLabel.setBounds(col, row * Screen.TILE_SIZE, Screen.TILE_SIZE, Screen.TILE_SIZE);
		this.add(pieceLabel);

		if (count > 1) {
			JLabel pieceCountLabel = new JLabel("x" + count);
			pieceCountLabel.setBounds(col + 2, row * Screen.TILE_SIZE - 4,
									  Screen.TILE_SIZE, Screen.TILE_SIZE / 4);
			this.add(pieceCountLabel);
		}
	}


	public void adapterEvent(int pieceRow, Coordinate endTile) {
		// Get piece type based on the row it should be displayed in
		Piece.Type pieceType = Piece.Type.NONE;
		switch (pieceRow) {
		case 3 -> pieceType = Piece.Type.PAWN;
		case 4 -> pieceType = Piece.Type.KNIGHT;
		case 5 -> pieceType = Piece.Type.BISHOP;
		case 6 -> pieceType = Piece.Type.ROOK;
		case 7 -> pieceType = Piece.Type.QUEEN;
		}

		Map<String, String> command = BugServer.cmdPlace(pieceType, endTile);
		String commandStr = Communication.serialize(command);

		// Notify action listeners
		ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, commandStr);
		for (ActionListener l : this.actionListeners)
			l.actionPerformed(actionEvent);
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

}
