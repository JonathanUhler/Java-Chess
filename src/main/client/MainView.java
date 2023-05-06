package client;


import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;


/**
 * Displays the main menu.
 */
public class MainView extends View {

	/** Hosts a new game. */
	private JButton hostButton;
	/** Joins an existing game. */
	private JButton joinButton;
	/** Displays the {@code InstView}. */
	private JButton instButton;
	/** Quits the game. */
	private JButton quitButton;


	/**
	 * Constructs a new {@code MainView} object.
	 *
	 * @param owner  a {@code Screen} object that manages this view.
	 */
	public MainView(Screen owner) {
		super(owner);

		this.hostButton = new JButton("Host Game");
		this.joinButton = new JButton("Join Game");
		this.instButton = new JButton("Instructions");
		this.quitButton = new JButton("Quit Game");

		this.hostButton.addActionListener(e -> this.hostAction());
		this.joinButton.addActionListener(e -> this.joinAction());
		this.instButton.addActionListener(e -> this.instAction());
		this.quitButton.addActionListener(e -> this.quitAction());

		super.redraw();
	}


	/**
	 * Invoked as the last step of drawing this {@code InstView}.
	 */
	@Override
	public void display() {
		OutlineLabel titleLabel = new OutlineLabel("Chess With Variants");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
		titleLabel.setForeground(new Color(255, 255, 255));
		titleLabel.setOutline(new Color(70, 70, 70));
		titleLabel.setOutlineSize(7);
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, Screen.TILE_SIZE, 0);
		this.add(titleLabel, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 0, 0);
		this.add(this.hostButton, gbc);

		gbc.gridy++;
		this.add(this.joinButton, gbc);

		gbc.gridy++;
		this.add(this.instButton, gbc);

		gbc.gridy++;
		this.add(this.quitButton, gbc);
	}


	/**
	 * Returns an user-defined IP address and port. If not null, the returned array is guaranteed
	 * to contain exactly two elements, where the first element can always be casted to a
	 * {@code String} and the second element can always be casted to an {@code Integer}. If
	 * the user closes the {@code JOptionPane} without selecting "OK", {@code null} is returned.
	 *
	 * @param prompt  the title of the displayed {@code JOptionPane} shown to the user.
	 *
	 * @return an user-defined IP address and port.
	 */
	private Object[] getConnectionInfo(String prompt) {
		JTextField ipTextField = new JTextField(8);
		JSpinner portSpinner = new JSpinner(new SpinnerNumberModel(1024, 1024, 49151, 1));
		JSpinner.NumberEditor portSpinnerEditor = new JSpinner.NumberEditor(portSpinner, "#");
		portSpinner.setEditor(portSpinnerEditor);
		JComponent[] components = new JComponent[] {new JLabel("IP Address:"),
													ipTextField,
													new JLabel("Port:"),
													portSpinner};

		int confirm = Chess.displayDialog(prompt, components);
		if (confirm != JOptionPane.OK_OPTION)
			return null;

		String ip = ipTextField.getText();
		int port = (Integer) portSpinner.getValue();

		return new Object[] {ip, port};
	}


	/**
	 * The action performed by the {@code hostButton} button.
	 */
	private void hostAction() {
	    Object[] connectionInfo = this.getConnectionInfo("Host Game");
		if (connectionInfo == null)
			return;
		
		String ip = (String) connectionInfo[0];
		int port = (Integer) connectionInfo[1];

		super.owner().displayGameView(ip, port, true);
	}


	/**
	 * The action performed by the {@code joinButton} button.
	 */
	private void joinAction() {
		Object[] connectionInfo = this.getConnectionInfo("Join Game");
		if (connectionInfo == null)
			return;
		
		String ip = (String) connectionInfo[0];
		int port = (Integer) connectionInfo[1];

		super.owner().displayGameView(ip, port);
	}


	/**
	 * The action performed by the {@code instButton} button.
	 */
	private void instAction() {
		super.owner().displayInstView();
	}


	/**
	 * The action performed by the {@code quitButton} button.
	 */
	private void quitAction() {
		System.exit(0);
	}

}
