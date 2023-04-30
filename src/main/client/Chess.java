// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Chess.java
// Networking-Chess
//
// Create by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package client;


import javax.swing.JFrame;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Chess
//
// Main runnable class to host a client
//
public class Chess {

	// ====================================================================================================
	// public static void main
	//
	// Main method, called on start
	//
	// Arguments--
	//
	//  args: command line arguments
	//
	public static void main(String[] args) {		
	    JFrame frame = new JFrame("Chess");
		GUI gui = new GUI();

		frame.add(gui);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	// end: public static void main
	
}
// end: public class Chess
