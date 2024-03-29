package client;


import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;


/**
 * Main class for the chess GUI.
 *
 * @author Jonathan Uhler
 */
public class Client {


	/** The main windows. */
	private static JFrame frame;


	/**
	 * Displays a graphical message.
	 *
	 * @param title    the title of the message.
	 * @param message  the message body to display.
	 */
	public static void displayMessage(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title,
									  JOptionPane.PLAIN_MESSAGE);
	}


	/**
	 * Displays a graphical message with arbitrary components.
	 *
	 * @param title       the title of the message.
	 * @param components  a list of components to display.
	 *
	 * @return a {@code JOptionPane.*_OPTION}.
	 */
	public static int displayDialog(String title, JComponent[] components) {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(200, components.length * 30));
		panel.setFocusable(true);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.gridwidth = GridBagConstraints.REMAINDER;

		for (int i = 0; i < components.length; i++)
			panel.add(components[i], g);

		return JOptionPane.showConfirmDialog(null, panel, title,
											 JOptionPane.OK_CANCEL_OPTION,
											 JOptionPane.PLAIN_MESSAGE, null);
	}


	/** 
	 * Repacks the main {@code JFrame}.
	 */
	public static void pack() {
		Client.frame.pack();
	}
	

	/**
	 * Main method for the chess GUI.
	 *
	 * @param args command line arguments.
	 */
	public static void main(String[] args) {		
	    Client.frame = new JFrame("Chess");
		Screen screen = new Screen();

		Client.frame.add(screen);

		Client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Client.frame.pack();
		Client.frame.setVisible(true);
	}
	
}
