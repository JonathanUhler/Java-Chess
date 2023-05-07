package client.view;


import jnet.Bytes;
import jnet.Log;
import client.Client;
import client.Screen;
import client.component.PiecePane;
import server.Server;
import server.Communication;
import engine.board.BoardInfo;
import engine.fen.FenUtility;
import engine.piece.Piece;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.util.Map;


public class ChessView extends GameView {

	public ChessView(Screen owner) {
		super(owner);
	}


	@Override
	public void startServer(String ip, int port) {
		Thread serverThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						ChessView.super.setServer(new Server(ip, port));
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
			piecePane.addActionListener(e -> this.piecePaneAction(e));
			super.setPiecePane(piecePane);
			break;
		}
		case Communication.CMD_STATE: {
			PiecePane piecePane = super.getPiecePane();
			if (piecePane == null) {
				Log.stdlog(Log.WARN, "ChessView", "PiecePane is null, ignoring state command");
				return;
			}

			String fenString = command.get(Communication.KEY_FEN);
			if (fenString == null) {
				Log.stdlog(Log.ERROR, "ChessView", "missing fen in state command: " + command);
				return;
			}

			BoardInfo info;
			try {
				info = FenUtility.informationFromFen(fenString);
			}
			catch (RuntimeException e) {
				Log.stdlog(Log.ERROR, "ChessView", "invalid fen position in state command: " + e);
				return;
			}
			
			piecePane.drawPosition(info);
			break;
		}
		default:
			Log.stdlog(Log.ERROR, "ChessView", "invalid client opcode in command: " + command);
			return;
		}
	}


	private void piecePaneAction(ActionEvent e) {
	    String commandStr = e.getActionCommand();
		super.getClientSocket().send(commandStr);
	}


	@Override
	public void display() {
		GridBagConstraints gbc = new GridBagConstraints();

		PiecePane piecePane = super.getPiecePane();
		gbc.gridx = 0;
		gbc.gridy = 0;
		if (piecePane != null)
			this.add(piecePane, gbc);
	}

}
