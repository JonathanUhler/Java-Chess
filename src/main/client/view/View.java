package client.view;


import client.Screen;
import javax.swing.JPanel;
import java.awt.GridBagLayout;


/**
 * {@code View} is a generic lightweight view. This class implements {@code JPanel} and can
 * perform any swing panel or component operation. In addition, this class is intended to
 * interface with a {@code Screen}, which can optionally be extended by an end-user
 * through modifications, allowing for custom chess rules and variants that require
 * special graphical components.
 * <p>
 * The various default views, most notably the {@code GameView} can be extended by programmers
 * to achieve greater functionality. Additionally, the move generation and movement system
 * can be modified similarly.
 *
 * @author Jonathan Uhler
 */
public abstract class View extends JPanel {

	/** A {@code Screen} object that manages this view. */
	private Screen owner;
	

	/**
	 * Constructs a new {@code View} object.
	 *
	 * @param owner  a {@code Screen} object that manages this view.
	 */
	public View(Screen owner) {
		this.setFocusable(true);
		this.setLayout(new GridBagLayout());
		
		this.owner = owner;
	}


	/**
	 * Returns this view's parent {@code Screen}.
	 *
	 * @return this view's parent {@code Screen}.
	 */
	public Screen owner() {
		return this.owner;
	}


	/**
	 * Redraws this view. Redrawing includes:
	 * <ul>
	 * <li> Clearing all child components of this view.
	 * <li> Revalidating this view.
	 * <li> Repainting this view.
	 * <li> Calling this view's {@code display} method to allow other graphical operations.
	 * </ul>
	 *
	 * @see display
	 */
	public void redraw() {
		this.removeAll();
		this.revalidate();
		this.repaint();
		this.display();
	}


	/**
	 * Invoked as the last step of drawing this {@code View}. Any component-level
	 * graphical operations can be performed in this method, notably {@code add}ing components.
	 */
	public abstract void display();

}
