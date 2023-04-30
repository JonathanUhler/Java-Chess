import javax.swing.*;

public class Runner {
	public static void main(String[] args) {
		JFrame f = new JFrame("test");
		Screen s = new Screen();
		f.add(s);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}
}
