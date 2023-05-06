package util;


/**
 * Represents a two-dimensional translational vector. This class is used to shift {@code Coordinate}
 * objects and represent the positions to which a piece can move as relative coordinates (that is, 
 * from an unknown starting tile).
 *
 * @author Jonathan Uhler
 */
public class Vector {

	/** The change in x position represented by this vector. */
	private int deltaX;
	/** The change in y position represented by this vector. */
	private int deltaY;
	

	/**
	 * Constructs a new {@code Vector} object.
	 *
	 * @param deltaX  the change in x position.
	 * @param deltaY  the change in y position.
	 */
	public Vector(int deltaX, int deltaY) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}


	/**
	 * Returns the change in x position.
	 *
	 * @return the change in x position.
	 */
	public int getXChange() {
		return this.deltaX;
	}
	

	/**
	 * Returns the change in y position.
	 *
	 * @return the change in y position.
	 */
	public int getYChange() {
		return this.deltaY;
	}


	/**
	 * Scales this {@code Vector} object by a given scale factor. This method returns a new object,
	 * leaving {@code this} unmodified. This method is identical to 
	 * {@code new Vector(getXChange() * scaleFactor, getYChange() * scaleFactor)}.
	 *
	 * @param scaleFactor  the multiplier to use for scaling this vector.
	 *
	 * @return a new {@code Vector} object scaled by {@code scaleFactor}.
	 */
	public Vector scale(int scaleFactor) {
		return new Vector(this.deltaX * scaleFactor, this.deltaY * scaleFactor);
	}
	

	/**
	 * Checks for equality between this {@code Vector} object and another object.
	 *
	 * @param obj  the object to check for equality with.
	 *
	 * @return true if the two object are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		// Try casting obj to a Vector object. If the cast fails, they cannot be equal
		Vector vObj;
		try {
		    vObj = (Vector) obj;
		}
		catch (ClassCastException e) {
			return false;
		}

		// If the object is null, it cannot be equal
		if (vObj == null)
			return false;

		// Check for the parameters of the Vector object for equality
		return (vObj.getXChange() == this.deltaX && vObj.getYChange() == this.deltaY);
	}


	/**
	 * Returns a string representation of this {@code Vector} object
	 *
	 * @return a string representation of this {@code Vector} object
	 */
	@Override
	public String toString() {
		return "<" + this.deltaX + ", " + this.deltaY + ">";
	}

}
