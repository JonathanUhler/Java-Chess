// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Coordinate.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package util;


import java.io.Serializable;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Coordinate implements Serializable
//
// Coordinate class to represent tiles on a chess board. Serializable to allow direct byte copying
//
public class Coordinate implements Serializable {

	private int x;
	private int y;
	

	// ----------------------------------------------------------------------------------------------------
	// public Coordinate
	//
	// Arguments--
	//
	//  x: x position
	//
	//  y: y position
	//
	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	// end: public Coordinate


	// ====================================================================================================
	// GET methods
	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}
	// end: GET methods


	// ====================================================================================================
	// public boolean isValidTile
	//
	// Returns whether this Coordinate object represents a valid tile on a chess board (0,0) to (7,7)
	//
	// Returns--
	//
	//  Whether the tile is valid
	//
	public boolean isValidTile() {
		if (this.x > 7 || this.x < 0 ||
			this.y > 7 || this.y < 0)
			return false;
		return true;
	}
	// end: public boolean isValidTile


	// ====================================================================================================
	// public Coordinate shift
	//
	// Shifts this Coordinate object by a Vector object v, returning a new Coordinate and leaving this
	// without any mutations
	//
	// Arguments--
	//
	//  v: the Vector object to shift by
	//
	// Returns--
	//
	//  A new Coordinate object: this shifted by Vector v
	//
	public Coordinate shift(Vector v) {
		return new Coordinate(this.x + v.getXChange(),
							  this.y + v.getYChange());
	}
	// end: public Coordinate shift


	// ====================================================================================================
	// public static Coordinate[] getAllValidCoordinates
	//
	// Returns an ordered list of all valid Coordinate objects on a chess board generated in the following
	// order:
	//
	//  - Starting at a1/(0,0)
	//  - Ending at h8/(7,7)
	//  - Moving across each row before going up (adding to x before y)
	//
	// Returns--
	//
	//  A list of Coordinate objects as described above
	//
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
	// end: public static Coordiante[] getAllValidCoordinates


	// ====================================================================================================
	// public boolean equals
	//
	// Checks equality against another object, mainly other Coordinate objects
	//
	// Arguments--
	//
	//  obj: an unknown object to compare equality with this Coordinate object
	//
	// Returns--
	//
	//  Whether the objects were determined to be equal
	//
	@Override
	public boolean equals(Object obj) {
		// Attempt to cast to a Coordinate object. If this fails, then obj is a different type, and thus
		// cannot be equal
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
	// end: public boolean equals


	// ====================================================================================================
	// public String toString
	//
	// Returns a string representation of this Coordinate object
	//
	// Returns--
	//
	//  A string of this object
	//
	@Override
	public String toString() {
		return "(" + this.x + "," + this.y + ") " + StringUtility.coordinateToString(this);
	}
	// end: public String toString

}
// end: public class Coordinate
