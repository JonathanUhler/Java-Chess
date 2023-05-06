// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// StringUtility.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package util;


import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class StringUtility
//
// Provides some string manipulation and serialization methods
//
public class StringUtility {

	// ====================================================================================================
	// public static String coordinateToString
	//
	// Converts a Coordinate object to a string representation in chess algebraic notation. If the
	// Coordinate object does not represent a valid tile on a chess board, the string "??" is returned
	// instead
	//
	// Arguments--
	//
	//  c: the Coordinate to convert
	//
	// Returns--
	//
	//  A string under the rules described above
	//
	public static String coordinateToString(Coordinate c) {
		if (c == null)
			throw new NullPointerException("coordinate was null");
		
		// The empty string "" is used in some places here to force char and int to cast to String. It might
		// not be necessary in all places, but ensures proper casting
		char column = (c.isValidTile()) ? (char)('a' + c.getX()) : ('?');
		String row = (c.isValidTile()) ? ((c.getY() + 1) + "") : "?";
		return column + "" + row;
	}
	// end: public static String coordinateToString


	// ====================================================================================================
	// public static Coordinate stringToCoordinate
	//
	// Converts a string in chess algebraic notation to a Coordinate object. The result is guaranteed
	// to be a valid tile on a chess board
	//
	// Arguments--
	//
	//  raw: the raw String object to convert
	//
	// Returns--
	//
	//  A coordinate object, or null if the string could not be parsed
	//
	public static Coordinate stringToCoordinate(String raw) {
		if (raw == null)
			throw new NullPointerException("raw string was null");
		
		// The String must be exactly two characters, a row and a column
		if (raw.length() != 2)
			throw new IllegalArgumentException("invalid raw string length: expected 2, found" + raw.length());

		char columnChar = raw.charAt(0);
		char rowChar = raw.charAt(1);

		// The second character (for the row) must be an integer
		if (!Character.isDigit(rowChar))
			return null;

		int column = columnChar - 'a';
		int row = Character.getNumericValue(rowChar) - 1;

		Coordinate coordinate = new Coordinate(column, row);
		if (!coordinate.isValidTile())
			throw new IllegalArgumentException("invalid raw string: " + raw);
		return coordinate;
	}
	// end: public static Coordinate stringToCoordinate
	

	// ====================================================================================================
	// public static Map<String, String> stringToMap
	//
	// Converts a 1-layer deep JSON-like string into a Map
	//
	// Arguments--
	//
	//  raw: the raw String to convert
	//
	// Returns--
	//
	//  A Map if successful, otherwise null
	//
	// Source: https://stackoverflow.com/questions/26485964/how-to-convert-string-into-hashmap-in-java
	public static Map<String, String> stringToMap(String raw) {
		try {
			String cleaned = raw.replaceAll("\"", "");
			cleaned = cleaned.replaceAll("[{}]", " ");
			Map<String, String> map = Arrays.stream(cleaned.split(","))
									  .map(s -> s.split(":", 2))
									  .collect(Collectors.toMap(s -> s[0].trim(), s -> s[1].trim()));
			return map;
		}
		catch (Exception e) {
		    throw new IllegalArgumentException("cannot parse raw string as map: " + e);
		}
	}
	// end: public static Map<String, String> stringToMap


	// ====================================================================================================
	// public static String mapToString
	//
	// Converts a Map into a JSON-like String
	//
	// Arguments--
	//
	//  map: the Map to convert
	//
	// Returns--
	//
	//  A JSON-like string if successful, otherwise null
	public static String mapToString(Map<String, String> map) {
		try {
			String raw = map
				.entrySet()
				.stream()
				.map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
				.collect(Collectors.joining(","));
			raw = "{" + raw + "}";
			return raw;
		}
		catch (Exception e) {
		    throw new IllegalArgumentException("cannot parse map as string: " + e);
		}
	}
	// end: public static String mapToString

}
// end: public class StringUtility
