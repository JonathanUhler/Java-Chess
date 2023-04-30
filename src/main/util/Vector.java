// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Vector.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package util;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Vector
//
// A two dimensional vector used to shift Coordinate objects
//
public class Vector {

	private int deltaX;
	private int deltaY;
	

	// ----------------------------------------------------------------------------------------------------
	// public Vector
	//
	// Arguments--
	//
	//  deltaX: the change in x
	//
	//  deltaY: the change in y
	//
	public Vector(int deltaX, int deltaY) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}
	// end: public Vector


	// ====================================================================================================
	// GET methods
	public int getXChange() {
		return this.deltaX;
	}

	public int getYChange() {
		return this.deltaY;
	}
	// end: GET methods


	// ====================================================================================================
	// public Vector scale
	//
	// Scales this Vector object by a given factor, returning a new Vector object in the process and
	// leaving this object without mutations
	//
	// Arguments--
	//
	//  scaleFactor: the multiplier to use for scaling this Vector
	//
	// Returns--
	//
	//  A new Vector object scaled by scaleFactor
	//
	public Vector scale(int scaleFactor) {
		return new Vector(this.deltaX * scaleFactor, this.deltaY * scaleFactor);
	}
	// end: public Vector scale
	

	// ====================================================================================================
	// public boolean equals
	//
	// Checks equality with another object, mainly another Vector
	//
	// Arguments--
	//
	//  obj: an unknown object to check equality with
	//
	// Returns--
	//
	//  Whether obj is equal to this object
	//
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
	// end: public boolean equals

	
	// ====================================================================================================
	// public String toString
	//
	// Returns a string representation of this Vector object
	//
	// Returns--
	//
	//  String of this object
	//
	@Override
	public String toString() {
		return "<" + this.deltaX + ", " + this.deltaY + ">";
	}
	// end: public String toString

}
// end: public class Vector
