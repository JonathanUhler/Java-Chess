package client;


import java.awt.Point;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;


// Source: https://stackoverflow.com/questions/27915214/how-can-i-drag-images-with-the-mouse-cursor-in-java-gui
public class MouseHandler extends MouseAdapter {

	private Point offset;
	

	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println("PRESSED");
		this.offset = e.getPoint();
	}

	
	@Override
	public void mouseReleased(MouseEvent e) {
		System.out.println("RELEASED");
	}

	
	@Override
	public void mouseDragged(MouseEvent e) {
		int x = e.getX() - this.offset.x;
		int y = e.getY() - this.offset.y;
		Component component = e.getComponent();

		Point location = component.getLocation();
		location.x += x;
		location.y += y;
		
		component.setLocation(location);
	}

}
