package client.component;


import javax.swing.JLabel;
import javax.swing.Icon;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.font.GlyphVector;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Color;
import java.awt.Dimension;


/**
 * A display area for a short text string with a highlight. A label does not react to input 
 * events. As a result, it cannot get the keyboard focus. A label can, however, display a keyboard 
 * alternative as a convenience for a nearby component that has a keyboard alternative but can't 
 * display it.
 * <p>
 * This extension of {@code JLabel} is not intended to support drawing images. You can specify 
 * where in the label's display area the label's contents are aligned by setting the vertical and 
 * horizontal alignment. By default, labels are vertically centered in their display area. 
 * Text-only labels are leading edge aligned, by default.
 * <p>
 * You can set the color and thickness of the text outline. The default color is black and the
 * default thickness is 1. A thickness value must be included, and no value {@code <= 0} is
 * allowed.
 *
 * @author Jonathan Uhler
 */
public class OutlineLabel extends JLabel {

	/** The color of the label's outline. */
	private Color outlineColor;
	/** The label's outline thickness, in pixels. */
	private int outlineSize;


	/**
	 * Constructs a new {@code OutlineLabel} object with an empty string for a title.
	 */
	public OutlineLabel() {
		super();
		this.init();
	}


	/**
	 * Constructs a new {@code OutlineLabel} object with the specified text.
	 *
	 * @param text  the text to be displayed by the label.
	 */
	public OutlineLabel(String text) {
		super(text);
		this.init();
	}


	/**
	 * Constructs a new {@code OutlineLabel} object with the specified text and horizontal
	 * alignment.
	 *
	 * @param text                 the text to be displayed by the label.
	 * @param horizontalAlignment  one of the following constants defined in 
	 *                             {@code SwingConstants}: {@code LEFT}, {@code CENTER}, 
	 *                             {@code RIGHT}, {@code LEADING} or {@code TRAILING}.
	 */
	public OutlineLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		this.init();
	}


	/**
	 * Initializes the label outline parameters. By default the color is black and the
	 * thickness is 1.
	 */
	private void init() {
		this.outlineColor = new Color(0, 0, 0);
		this.outlineSize = 1;
	}


	/**
	 * Returns this label's outline color.
	 *
	 * @return this label's outline color.
	 */
	public Color getOutline() {
		return this.outlineColor;
	}


	/**
	 * Returns this label's outline thickness. The returned value is in pixels.
	 *
	 * @return this label's outline thickness.
	 */
	public int getOutlineSize() {
		return this.outlineSize;
	}


	/**
	 * Sets this label's outline color. If the color is {@code null}, the call is ignored.
	 *
	 * @param outlineColor  the color to become this label's outline color.
	 */
	public void setOutline(Color outlineColor) {
		if (outlineColor == null)
			return;
		this.outlineColor = outlineColor;
		this.revalidate();
	}


	/**
	 * Sets this label's outline size. The size should be given in pixels. Any positive size
	 * can be passed as an argument, although large sizes (especially when larger than the
	 * font size for this label) may cause graphical issues.
	 *
	 * @param outlineSize  the size to become this label's outline thickness.
	 *
	 * @throws IllegalArgumentException  if {@code outlineSize <= 0}
	 */
	public void setOutlineSize(int outlineSize) {
		if (outlineSize <= 0)
		    throw new IllegalArgumentException("outlineSize must be > 0, found " + outlineSize);
	    this.outlineSize = outlineSize;
		this.revalidate();
	}


	/**
	 * Paints this {@code OutlineLabel} object with the given outline color and size. In addition,
	 * all {@code JLabel} operations are honored regarding font and foreground color.
	 *
	 * @param g  the {@code Graphics} object to paint to.
	 */
	@Override
	public void paintComponent(Graphics g) {
		String text = super.getText();
		if (text == null || text.length() == 0) {
			super.paintComponent(g);
			return;
		}

		Graphics2D gg = (Graphics2D) g.create();
		gg.translate(0, 4 * g.getFontMetrics().getHeight() / 5);

		GlyphVector gv = super.getFont().createGlyphVector(gg.getFontRenderContext(), text);
		Shape shape = gv.getOutline();

		gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		gg.setColor(this.outlineColor);
		gg.setStroke(new BasicStroke((float) this.outlineSize));
		gg.draw(shape);

		gg.setColor(super.getForeground());
		gg.fill(shape);

		gg.dispose();
	}

}
