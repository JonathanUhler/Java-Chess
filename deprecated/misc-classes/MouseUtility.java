/*
package util;


import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.JLabel;


public class MouseUtility extends MouseAdapter {

	private Point offset;


	@Override
	public void mousePressed(MouseEvent e) {
		JLabel label = (JLabel) e.getComponent();
		this.offset = e.getPoint();
		System.out.println("PRESSED");
	}


	@Override
	public void mouseDragged(MouseEvent e) {
		System.out.println("DRAGGED");
		int x = e.getPoint().x - this.offset.x;
		int y = e.getPoint().y - this.offset.y;

		Component component = e.getComponent();
		Point location = component.getLocation();

		location.x += x;
		location.y += y;

		component.setLocation(location);
	}
	

						pieceImage.addMouseListener(new MouseInputAdapter() {
								@Override
								public void mousePressed(MouseEvent e) {
									System.out.println("PRESSED");
									GUI.this.pieceClicked(e, pieceImage);
								}
								@Override
								public void mouseReleased(MouseEvent e) {
									System.out.println("RELEASED");
									GUI.this.pieceReleased(e, pieceImage);
								}
								// MIGHT NEED TO PUT DRAGGED METHOD IN A mouseMotionListener INSTEAD OF
								// THIS mouseListener, BUT THERE IS STILL THE ISSUE WITH RELEASED NOT
								// WORKING WHEN IT SHOULD
								@Override
								public void mouseDragged(MouseEvent e) {
									System.out.println("DRAGGED");
									GUI.this.pieceDragged(e, pieceImage);
								}
							});
}
*/
