
// *********************************************************************************************************
//SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION

//- Use null layout manager to allow the released/dragged to work
//- To display the buttons and stuff on top, need to create a second JPanel and add that, then keep the layout manager
//  of that second panel to the default (not null)

//SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION | SOLUTION
// *********************************************************************************************************

// QUESTIONS
// -----------
//SOLVED: HOW do the setBounds calls in GUI work when there is no null layout manager????? Why do they not work
//        in this file but do in GUI????????????????????
//        ANS: in this I did not override paintComponent properly so it wasn't getting called. With that fixed,
//             the pieces now show up. That doesn't really explain WHY setBounds works, but it must just be
//             overwriting the position set by the layout manager initially
//
//SOLVED: Why when the layout manager is null does the click work?????????????????????????
//        ANS: just because
//
//SOLVED-ISH: Why does only the pressed but not released work in GUI??????????????????????
//            ANS: just because, non-null manager messes with mouse stuff
// -----------
// QUESTIONS
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.Map;





/*
public class Screen extends JPanel {

	// Margins and sizes
	//private static final int MARGIN = 40;
	//private static final int TILE_SIZE = 70;

	private JLayeredPane pieces;
	

	public Screen() {
		// NOTE: Without this line, the piece does not seem to show up at all
		this.setPreferredSize(new Dimension(500, 500));
		//JTextField ipTextField = new JTextField(10);
		//JTextField portTextField = new JTextField(4);
		//JButton hostButton = new JButton("Host Game");
		JButton joinButton = new JButton("Join Game");
		//this.add(new JLabel("IP Addr:"));// NOT THIS
		//this.add(ipTextField);// NOT THIS
		//this.add(new JLabel("Port:"));// NOT THIS
		//this.add(portTextField);// NOT THIS
		//this.add(hostButton);// NOT THIS
		this.add(joinButton);// NOT THIS
		this.pieces = new JLayeredPane();
		this.drawPosition();
	}
	

	private void drawPosition() {
		// REMOVING THIS LINE FIXES THE MOUSE RELEASED ISSUE
		this.pieces.removeAll();
		// REMOVING THIS LINE FIXES THE MOUSE RELEASED ISSUE

		
		this.pieces.setBounds(0, 0, this.getPreferredSize().width, this.getPreferredSize().height); // MARK: TO DO: does this need to be here? or can it be in the constructor


		
		// displayOffsetY used to reflect the board across the x-axis depending on which perspective
		// is being viewed
		for (int i = 0; i < 2; i++) {
			int x = 80 + (i * 100);
			int y = 120;
			int size = 70;

			// MARK: TO DO: add these images to the jar and reference them from there
			JLabel pieceImage = new JLabel(new ImageIcon("/Users/jonathan/Documents/Computer-Science/Java/Networking-Chess/src/reference/12.png"));
			pieceImage.setBounds(x, y, size, size);

			pieceImage.addMouseListener(new MouseInputAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						System.out.println("PRESSED");
					}
					@Override
					public void mouseReleased(MouseEvent e) {
						System.out.println("RELEASED");
					}
				});
			pieceImage.addMouseMotionListener(new MouseInputAdapter() {
					@Override
					public void mouseDragged(MouseEvent e) {
						System.out.println("DRAGGED");
						pieceImage.setLocation(e.getX(), e.getY());
					}
				});
			this.pieces.add(pieceImage);
		}


		
		this.add(this.pieces); // MARK: TO DO: --------------------------------------------does this need to be here? or can it be in the constructor
		this.repaint();
	}
	@Override
	public void paintComponent(Graphics g) {
		//g.drawString("Position: ----", 50,// NOT THIS
		//			 Screen.MARGIN + (Screen.TILE_SIZE * 8) + (int)(Screen.MARGIN / 1.5));// NOT THIS

		this.drawPosition();
	}

}
*/






public class Screen extends JPanel {
	public JLayeredPane pane;
	public Screen() {
		this.setPreferredSize(new Dimension(400, 400));
		JButton b = new JButton("test");
		this.add(b);
		this.pane = new JLayeredPane();
		test();
	}


	public void test() {
		this.pane.removeAll();
		this.pane.setBounds(0, 0, this.getPreferredSize().width, this.getPreferredSize().height);

		for (int i = 0; i < 2; i++) {
			JLabel l = new JLabel(new ImageIcon("/Users/jonathan/Documents/Computer-Science/Java/Networking-Chess/src/reference/12.png"));
			int x = 80 + (i * 100);
			int y = 120;
			int size = 70;
			l.setBounds(x, y, size, size);
			l.addMouseListener(new MouseInputAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						System.out.println("PRESSED");
					}
					@Override
					public void mouseReleased(MouseEvent e) {
						System.out.println("RELEASED");
					}
				});
			l.addMouseMotionListener(new MouseInputAdapter() {
					@Override
					public void mouseDragged(MouseEvent e) {
						System.out.println("DRAGGED");
						l.setLocation(e.getX(), e.getY());
					}
				});
			this.pane.add(l);
		}
		
		this.add(this.pane);
		this.repaint();
	}

	
	@Override
	public void paintComponent(Graphics g) {
		test();
	}
}

