package util;


import java.io.Serializable;


/**
 * Represents a tile on the chess board. This class implements the {@code Serializable} interface
 * to allow deep-copying by the {@code BoardInfo} class.
 *
 * @author Jonathan Uhler
 */
public class Coordinate implements Serializable {

	/** x position of the coordinate. */
	private int x;
	/** y position of the coordinate. */
	private int y;
	

	/**
	 * Constructs a {@code Coordinate} object.
	 *
	 * @param x  x position.
	 * @param y  y position.
	 */
	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}


	/**
	 * Returns the x position of this coordinate.
	 *
	 * @return the x position of this coordinate.
	 */
	public int getX() {
		return this.x;
	}


	/**
	 * Returns the y position of this coordinate.
	 *
	 * @return the y position of this coordinate.
	 */
	public int getY() {
		return this.y;
	}


	/**
	 * Determines whether this {@code Coordinate} object represents a valid tile on a chess board. 
	 * To be valid, the coordinate should be on the interval [0, 7] on both the x and y axes.
	 *
	 * @return true if this {@code Coordinate} object represents a valid tile.
	 */
	public boolean isValidTile() {
		return this.x >= 0 && this.x <= 7 &&
			   this.y >= 0 && this.y <= 7;
	}


	/**
	 * Moves a {@code Coordinate} object by a {@code Vector} object. This method returns a new
	 * {@code Coordinate} object, leaving {@code this} unmodified. This method is identical to:
	 * <p>
	 * {@code new Coordinate(getX() + v.getXChange(), getY() + v.getYChange())}
	 *
	 * @param v  the {@code Vector} object to shift by
	 *
	 * @return a new {@code Coordinate} object translated by the vector {@code v}.
	 */
	public Coordinate shift(Vector v) {
		return new Coordinate(this.x + v.getXChange(),
							  this.y + v.getYChange());
	}


	/**
	 * Returns an ordered list of all valid {@code Coordinate} objects. In other words, this method
	 * returns the set of all coordinates {@code c} where {@code (c).isValidTile() == true} for 
	 * all elements and {@code (c').isValidTile() == false}.
	 * <p>
	 * The coordinate list is generated in the following way:
	 * <ul>
	 * <li> Coordinates start at A1 (0, 0)
	 * <li> Coordinates end at H8 (7, 7)
	 * <li> Coordinates move across each coumn before going up to the next row 
	 *      (adding to x before y)
	 * </ul>
	 *
	 * @return a list of all valid coordiantes.
	 */
	public static Coordinate[] getAllValidCoordinates() {
		Coordinate[] allValidCoordinates = new Coordinate[64];

		int i = 0;
		// Loop through y before x, because the loop should be row-major order and y=row/x=cols
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				allValidCoordinates[i] = new Coordinate(x, y);
				i++;
			}
		}

		return allValidCoordinates;
	}


	/**
	 * Checks for equality between this {@code Coordinate} object and another object.
	 *
	 * @param obj  the object to compare the equality of.
	 *
	 * @return true if the two objects are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		// Attempt to cast to a Coordinate object. If this fails, then obj is a different type,
		// and thus cannot be equal
		Coordinate cObj;
		try {
		    cObj = (Coordinate) obj;
		}
		catch (ClassCastException e) {
			return false;
		}

		// If the coordinate (or obj to begin) is null, then there is no equality
		if (cObj == null)
			return false;

		// Check the parameters of Coordinate to determine equality
		return (cObj.getX() == this.x && cObj.getY() == this.y);
	}


	/**
	 * Returns a string representation of this {@code Coordinate} object.
	 *
	 * @return a string representation of this {@code Coordinate} object.
	 */
	@Override
	public String toString() {
		return "(" + this.x + "," + this.y + ") " + StringUtility.coordinateToString(this);
	}

}
