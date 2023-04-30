package client;


import util.Coordinate;
import javax.swing.JLabel;
import javax.swing.ImageIcon;


public class PieceImage extends JLabel {

	private Coordinate tile;
	

	public PieceImage(ImageIcon icon, Coordinate tile) {
		super.setIcon(icon);
		super.setHorizontalAlignment(JLabel.CENTER);
		super.setVerticalAlignment(JLabel.CENTER);
		this.tile = tile;
	}


	public Coordinate getTile() {
		return this.tile;
	}

}
