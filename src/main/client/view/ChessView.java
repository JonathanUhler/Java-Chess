package client.view;


import jnet.Bytes;
import jnet.Log;
import client.Client;
import client.Screen;
import client.sound.SoundManager;
import client.component.PiecePane;
import server.ChessServer;
import server.Communication;
import engine.board.BoardInfo;
import engine.fen.FenUtility;
import engine.piece.Piece;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.util.Map;


/**
 * A {@code GameView} to manage a classic game of chess.
 *
 * @author Jonathan Uhler
 */
public class ChessView extends GameView implements ActionListener {

	/** Whether the current game is still active. */
	private boolean playing;

	/** Button to restart the game after finished. */
	private JButton restartButton;
	/** Button to quit the game after finished. */
	private JButton quitButton;
	

	/**
	 * Constructs a new {@code ChessView} object.
	 *
	 * @param owner  a {@code Screen} object that manages this view.
	 */
	public ChessView(Screen owner) {
		super(owner);
		this.playing = true;

		this.restartButton = new JButton("New Game");
		this.quitButton = new JButton("Quit");

		this.restartButton.addActionListener(e -> this.restartAction());
		this.quitButton.addActionListener(e -> this.quitAction());
		
		super.redraw();
	}


	@Override
	public void startServer(String ip, int port) {
		Thread serverThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						ChessView.super.setServer(new ChessServer(ip, port));
					}
					catch (IOException e) {
						Client.displayMessage("Connection Error", "Unable to start server on " +
											  ip + ":" + port + ".\n" + e);
						Thread.currentThread().interrupt();
						return;
					}
				}
			});
		serverThread.start();
	}


	@Override
	public void serverCommunicated(byte[] recv) {
		String commandStr = Bytes.bytesToString(recv);
		Map<String, String> command = Communication.deserialize(commandStr);
		String opcode = command.get(Communication.KEY_CMD);
		if (opcode == null) {
		    Log.stdlog(Log.ERROR, "ChessView", "null opcode in command: " + command);
			return;
		}

		switch (opcode) {
			// If this is the first communication with the server, the client will be
			// assigned a color (one of the enum parameters of Piece.Color). When this
			// happens, the piece pane must be initialized.
		case Communication.CMD_COLOR: {
			// Check if this is actually the first communication with the server (e.g. the
			// piece pane has not been set up yet)
			if (super.getPiecePane() != null) {
				Log.stdlog(Log.WARN, "ChessView",
						   "PiecePane is already initialized, ignoring color command: " + command);
				return;
			}

			// Get the assigned color
			Piece.Color playerColor;
			try {
				String playerColorStr = command.get(Communication.KEY_COLOR);
				playerColor = Piece.Color.valueOf(playerColorStr);
			}
			catch (RuntimeException e) {
				Log.stdlog(Log.ERROR, "ChessView", "cannot parse color in command: " + command);
				return;
			}

			// Create the piece pane
            PiecePane piecePane = new PiecePane(playerColor);
			piecePane.addActionListener(this);
			super.setPiecePane(piecePane);
			break;
		}
		case Communication.CMD_STATE: {
			this.playing = true;
			
			// Check the piece pane
			PiecePane piecePane = super.getPiecePane();
			if (piecePane == null) {
				Log.stdlog(Log.WARN, "ChessView", "PiecePane is null, ignoring state command");
				return;
			}

			// Get the fen string
			String fenString = command.get(Communication.KEY_FEN);
			if (fenString == null) {
				Log.stdlog(Log.ERROR, "ChessView", "missing fen in state command: " + command);
				return;
			}

			// Handle the state (display game status to the user if the game ended)
			String stateStr = command.get(Communication.KEY_STATE);
			BoardInfo.State state = null;
			try {
				state = BoardInfo.State.valueOf(stateStr);
			}
			catch (RuntimeException e) {
				Log.stdlog(Log.ERROR, "ChessView", "invalid state in state command: " + state);
				return;
			}

			if (state != BoardInfo.State.ONGOING) {
				this.playing = false;
				SoundManager.playSound("game");
				Client.displayMessage("Game Finished", "Game finished: " + state.name());
			}

			// Draw the position
			BoardInfo info;
			try {
				info = FenUtility.informationFromFen(fenString);
			}
			catch (RuntimeException e) {
				Log.stdlog(Log.ERROR, "ChessView", "invalid fen position in state command: " + e);
				return;
			}
			piecePane.drawPosition(info, this.playing);
			break;
		}
		default:
			Log.stdlog(Log.ERROR, "ChessView", "invalid client opcode in command: " + command);
			return;
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// Play piece moved sound
		SoundManager.playSound("move");

		// Send move command
		String commandStr = e.getActionCommand();
		super.getClientSocket().send(commandStr);
	}


	private void restartAction() {
		Map<String, String> command = Communication.cmdRestart();
		super.getClientSocket().send(Communication.serialize(command));
	}


	private void quitAction() {
		super.closeServer();
		super.owner().displayMainView();
	}


	@Override
	public void display() {
		GridBagConstraints gbc = new GridBagConstraints();

		PiecePane piecePane = super.getPiecePane();
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		if (piecePane != null)
			this.add(piecePane, gbc);

		if (!this.playing) {
			gbc.gridwidth = 1;
			gbc.gridy++;
			this.add(this.restartButton, gbc);
			
			gbc.gridx++;
			this.add(this.quitButton, gbc);
		}
	}


	@Override
	public String toString() {
		return "Classic Chess";
	}

}
