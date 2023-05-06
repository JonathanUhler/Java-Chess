package client;


import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;


/**
 * Displays game instructions.
 */
public class InstView extends View {

	/** The game instructions as HTML. */
	public static final String INSTRUCTIONS =
"<html>" +
" <head>" +
"  <style>" +
"   h1 {font-family:Arial}" +
"   h2 {font-family:Arial}" +
"   p {font-family:Arial}" +
"   ul {font-family:Arial}" +
"   li {font-family:Arial}" +
"   code {font-family:Courier New}" +
"  </style>" +
" </head>" +
" <body>" +
"  <h1>Hosting and Joining Games</h1>" +
"  <p> On the main screen of the game, select \"Host Game\" or \"Join Game\" to begin." +
"  <p> This graphical user interface will automatically run a new server instance on the" +
"      desired IP and port combination upon selecting \"Host Game\". After the server is" +
"      started, your client will be connected to the same IP address and port. If any" +
"      network errors occur, a message will be displayed. Please read the error description" +
"      and follow any provided steps to remedy the issue." +
"  <p> If you wish to join an existing game, select \"Join Game\". Enter the IP address" +
"      and port the game is being hosted on (ask the host if you are unsure of this information)." +
"      You will be connected to the game if able. If an error occurs, a message will be shown." +
"  <h1>Playing Chess</h1>" +
"  <h2>Basic Rules</h2>" +
"  <p> For basic information on how to play chess, please see: <a href=https://www.chess.com/article/view/how-to-play-chess>https://www.chess.com/article/view/how-to-play-chess</a>" +
"  <p> This game implements a full chess engine, which will prevent you (and your opponents)" +
"      from making \"illegal\" moves. Only fully-legal moves are allowed by the engine," +
"      including accounting for check and checkmate. For your convenience, legal moves for" +
"      a given piece will be highlighted red on the chess board when you pick up a piece with" +
"      the mouse." +
"  <p> To move pieces, click and drag an image of one of the pieces. To place the piece on the" +
"      desired square, move your cursor (with the piece attached) over the tile and release" +
"      the left mouse button." +
"  <p> If the move you make is illegal, the piece will snap back to its original location" +
"      and you can attempt to make another, legal move. Otherwise, the piece will be placed" +
"      on the desired tile." +
"  <h2>Special Rules</h2>" +
"  <p> This chess engine supports all traditional, although obscure, chess rules. These include:" +
"  <ul>" +
"   <li>En passant" +
"   <li>Castling" +
"   <li>Promotion" +
"  </ul>" +
"  <p> Note: when promoting pawns, a separate <code>JOptionPane</code> will be displayed." +
"      Selected the desired piece type and press \"OK\" to promote." +
"  <h1>Playing Crazyhouse</h1>" +
"  <p> In addition to the regular rules of chess, the crazyhouse variant allows players" +
"      to keep and place pieces they capture." +
"  <p> After capturing pieces from your opponent, you will recieve a copy of the same piece" +
"      type with your color (such that you can use it against your opponent). During any of" +
"      your turns, if you wish, you may place a single piece from your \"bank\" instead of" +
"      moving a piece already on the board." +
"  <p> To place a piece, click and drag the type of piece from the left side of the screen" +
"      and place it on any tile with the following restrictions:" +
"  <ul>" +
"   <li>Pawns cannot be placed on the back-most rank and be immediately promoted." +
"   <li>Pieces can only be placed on empty tiles." +
"  </ul>" +
" </body>" +
"</html>";

	/** The pane to which the instructions are drawn. */
	private JTextPane instTextPane;
	/** A button to return to the main view. */
	private JButton backButton;
	

    /**
	 * Constructs a new {@code InstView} object.
	 *
	 * @param owner  a {@code Screen} object that manages this view.
	 */
    public InstView(Screen owner) {
		super(owner);

		this.instTextPane = new JTextPane();
		this.backButton = new JButton("Back");

		this.instTextPane.setPreferredSize(new Dimension(Screen.TILE_SIZE * 7,
														 Screen.TILE_SIZE * 6));
		this.instTextPane.setEditable(false);
		this.instTextPane.setContentType("text/html");

		this.instTextPane.setText(InstView.INSTRUCTIONS);
		this.backButton.addActionListener(e -> this.backAction());

		super.redraw();
	}


	/**
	 * Invoked as the last step of drawing this {@code InstView}.
	 */
	@Override
	public void display() {
		OutlineLabel titleLabel = new OutlineLabel("Game Instructions");
		titleLabel.setFont(new Font("Arial", Font.PLAIN, 40));
		titleLabel.setForeground(new Color(255, 255, 255));
		titleLabel.setOutline(new Color(70, 70, 70));
		titleLabel.setOutlineSize(5);
		
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		this.add(titleLabel, gbc);

		gbc.gridy++;
		this.add(new JScrollPane(this.instTextPane), gbc);

		gbc.gridy++;
		this.add(this.backButton, gbc);
	}


	/**
	 * The action performed by the {@code backButton} button.
	 */
	private void backAction() {
		super.owner().displayMainView();
	}

}
