package client;


import client.view.MainView;
import client.view.InstView;
import client.view.GameView;
import javax.swing.JPanel;
import java.awt.Dimension;


/**
 * Manages the graphical user interface for the chess application.
 */
public class Screen extends JPanel {

	/** The size of a tile on the chess board, in pixels. */
	public static final int TILE_SIZE = 70;

	/** The main view. */
    private MainView mainView;
	/** The instructions view. */
    private InstView instView;


	/**
	 * Constructs a new {@code Screen} object.
	 */
	public Screen() {
		this.mainView = new MainView(this);
		this.instView = new InstView(this);

		this.displayMainView();
	}


	/**
	 * Redraws this screen, removing whatever {@code View} that might have been added.
	 */
	private void clearGraphicsContext() {
		this.removeAll();
		this.revalidate();
		this.repaint();
	}


	/**
	 * Displays the main view.
	 */
	public void displayMainView() {
		this.clearGraphicsContext();
		this.add(this.mainView);
	}


	/**
	 * Displays the instructions view.
	 */
	public void displayInstView() {
		this.clearGraphicsContext();
		this.add(this.instView);
	}


	/**
	 * Displays the game view after joining an existing game.
	 *
	 * @param gameView  the game view to display.
	 * @param ip        the IP address to join the game on.
	 * @param port      the port to join the game on.
	 */
	public void displayGameView(GameView gameView, String ip, int port) {
	    this.displayGameView(gameView, ip, port, false);
	}


	/**
	 * Displays the game view after hosting and/or joining a new game.
	 *
	 * @param gameView  the game view to display.
	 * @param ip        the IP address to host/join the game on.
	 * @param port      the port to host/join the game on.
	 * @param hosting   whether a new game should be hosted, or an existing game joined.
	 */
	public void displayGameView(GameView gameView, String ip, int port, boolean hosting) {
		this.clearGraphicsContext();
		gameView.connect(ip, port, hosting);
		this.add(gameView);
	}

}
