package server;


import engine.piece.Piece;
import engine.move.Move;
import engine.board.BoardInfo;
import engine.fen.FenUtility;
import java.util.Map;
import java.util.HashMap;


/**
 * Facilitates communication over the network with strings and maps.
 *
 * @author Jonathan Uhler
 */
public class Communication {

	/** Value indicating a color assignemnt command. */
	public static final String CMD_COLOR = "color";
	/** Value indicating a move command. */
	public static final String CMD_MOVE = "move";
	/** Value indicating a board state command. */
	public static final String CMD_STATE = "state";
	/** Key indicating the type of command sent. */
	public static final String KEY_CMD = "cmd";
	/** Key indicating the color of the player in the scope of the command. */
	public static final String KEY_COLOR = "color";
	/** Key indicating the start tile of a move. */
	public static final String KEY_START = "start";
	/** Key indicating the end tile of a move. */
	public static final String KEY_END = "end";
	/** Key indicating the special flag of a move. */
	public static final String KEY_FLAG = "flag";
	/** Key indicating the board state as a fen string. */
	public static final String KEY_FEN = "fen";
	/** Key indicating the state of the board (as defined by the {@code BoardInfo.State} enum. */
	public static final String KEY_STATE = "state";


	private Communication() { }
	

	/**
	 * Generates the payload for a color assignment command. A {@code null} value is returned
	 * if the argument if null, otherwise a {@code Map} as described below is returned.
	 * <p>
	 * This command is comprised of the following components:
	 * <table style="border: 1px solid black">
	 *  <caption>{@code color} Command Payload</caption>
	 *  <tr style="border: 1px solid black">
	 *   <th style="border: 1px solid black"> Key
	 *   <th style="border: 1px solid black"> Commentary
	 *  </tr>
	 *  <tr style="border: 1px solid black">
	 *   <td style="border: 1px solid black"> {@code cmd}
	 *   <td style="border: 1px solid black"> Identifies this command, always {@code color}.
	 *  </tr>
	 *  <tr style="border: 1px solid black">
	 *   <td style="border: 1px solid black"> {@code color}
	 *   <td style="border: 1px solid black"> The color being assigned by this command. This value
	 *                                        is the result of a {@code .name()} call to one of
	 *                                        the parameters of the {@code Piece.Color} enum.
	 *                                        The string includes any capitalization of the
	 *                                        parameter, and is case sensative for deserialization.
	 *  </tr>
	 * </table>
	 *
	 * @param color  the color to act as the value for the {@code color} entry.
	 *
	 * @return the payload for a color assignment command.
	 */
	public static Map<String, String> cmdColor(Piece.Color color) {
		if (color == null)
			return null;
		
		Map<String, String> map = new HashMap<>();
		map.put(Communication.KEY_CMD, Communication.CMD_COLOR);
		map.put(Communication.KEY_COLOR, color.name());
		return map;
	}



	/**
	 * Generates the payload for a movement command. A {@code null} value is returned if the
	 * argument if null, otherwise a {@code Map} as described below is returned.
	 * <p>
	 * This command is comprised of the following components:
	 * <table style="border: 1px solid black">
	 *  <caption>{@code move} Command Payload</caption>
	 *  <tr style="border: 1px solid black">
	 *   <th style="border: 1px solid black"> Key
	 *   <th style="border: 1px solid black"> Commentary
	 *  </tr>
	 *  <tr style="border: 1px solid black">
	 *   <td style="border: 1px solid black"> {@code cmd}
	 *   <td style="border: 1px solid black"> Identifies this command, always {@code move}.
	 *  </tr>
	 *  <tr style="border: 1px solid black">
	 *   <td style="border: 1px solid black"> {@code start}
	 *   <td style="border: 1px solid black"> The start tile, in algebraic notation, of the move
	 *                                        object. Algebraic notation consists of a two
	 *                                        character string, where the first character is a
	 *                                        lowercase letter on the interval [a, h] representing
	 *                                        the column, and the second character is a number
	 *                                        on the interval [1, 8] representing the row.
	 *  </tr>
	 *  <tr style="border: 1px solid black">
	 *   <td style="border: 1px solid black"> {@code end}
	 *   <td style="border: 1px solid black"> The end tile, in algebraic notation, of the move.
	 *  </tr>
	 *  <tr style="border: 1px solid black">
	 *   <td style="border: 1px solid black"> {@code flag}
	 *   <td style="border: 1px solid black"> The flag of the move. This value is a string literal
	 *                                        resulting from the {@code .name()} call to the
	 *                                        {@code Move.Flag} enum parameter contained by the
	 *                                        move object. If the flag is none, the value
	 *                                        {@code "NONE"} will still be used.
	 *  </tr>
	 * </table>
	 *
	 * @param move  the move to act as the value for parameters in this command.
	 *
	 * @return the payload for a movement command.
	 */
	public static Map<String, String> cmdMove(Move move) {
		if (move == null)
			return null;
		
	    Map<String, String> map = new HashMap<>();
		map.put(Communication.KEY_CMD, Communication.CMD_MOVE);
	    map.put(Communication.KEY_START, move.getStartTile().toString());
		map.put(Communication.KEY_END, move.getEndTile().toString());
		map.put(Communication.KEY_FLAG, move.getFlag().name());
		return map;
	}


	/**
	 * Generates the payload for a board state command. A {@code null} value is returned if the
	 * argument if null, otherwise a {@code Map} as described below is returned.
	 * <p>
	 * This command is comprised of the following components:
	 * <table style="border: 1px solid black">
	 *  <caption>{@code state} Command Payload</caption>
	 *  <tr style="border: 1px solid black">
	 *   <th style="border: 1px solid black"> Key
	 *   <th style="border: 1px solid black"> Commentary
	 *  </tr>
	 *  <tr style="border: 1px solid black">
	 *   <td style="border: 1px solid black"> {@code cmd}
	 *   <td style="border: 1px solid black"> Identifies this command, always {@code state}.
	 *  </tr>
	 *  <tr style="border: 1px solid black">
	 *   <td style="border: 1px solid black"> {@code fen}
	 *   <td style="border: 1px solid black"> The fen string representing the argument object.
	 *                                        This is derived using the 
	 *                                        {@code FenUtility.fenFromInformation} method.
	 *  </tr>
	 *  <tr style="border: 1px solid black">
	 *   <td style="border: 1px solid black"> {@code state}
	 *   <td style="border: 1px solid black"> The current state of the board as determined by the
	 *                                        {@code inferState} method of the argument
	 *                                        {@code BoardInfo} object. The value for this key
	 *                                        is a string literal resulting from the call to
	 *                                        the {@code .name()} method of the returned
	 *                                        {@code BoardInfo.State} enum parameter.
	 *  </tr>
	 * </table>
	 *
	 * @param info  the board information to act as the value for parameters in this command.
	 *
	 * @return the payload for a board state command.
	 */
	public static Map<String, String> cmdState(BoardInfo info) {
		if (info == null)
			return null;
		
	    Map<String, String> map = new HashMap<>();
		map.put(Communication.KEY_CMD, Communication.CMD_STATE);
	    map.put(Communication.KEY_FEN, FenUtility.fenFromInformation(info));
		map.put(Communication.KEY_STATE, info.inferState().name());
		return map;
	}
	

	/**
	 * Serializes a {@code Map} to a {@code String}. The keys and values of the map are
	 * permitted to contain any character that can be successfully passed over a network
	 * socket. Commas, colons, and double-quotes are allowed and will be managed accordingly
	 * by the {@code deserialize} method.
	 * <p>
	 * This method does not rely on the {@code Map.toString} method. The syntax of the returned
	 * string is as follows:
	 * <ul>
	 * <li> The string begins and ends with curly brackets
	 * <li> All keys and values, which must be strings, are enclosed within literal double-quotes
	 * <li> Any double-quotes already present in the keys and values are converted to the
	 *      literal string '\"' (that is, the value {@code \\\\\"} is inserted to replace any 
	 *      {@code \"})
	 * <li> Keys are separated from values with a single colon, and without any whitespace
	 * <li> Map entries are separated with a single comma, and without any whitespace
	 * </ul>
	 * <p>
	 * An example string for the map {@code {key=value, ke"y=value}}, when printed to
	 * the standard output,  would be:
	 * <p>
	 * {@code '{"key":"value","ke\"y"="value"}'}
	 *
	 * @param map  the {@code Map} to serialize.
	 *
	 * @return the serialized string.
	 *
	 * @see deserialize
	 */
	public static String serialize(Map<String, String> map) {
		String str = "{";

		// Add each key and value pair
		for (String key : map.keySet()) {
			String value = map.get(key);

			// Replace all quote literals in the strings with a literal \" to prevent
			// confusion when deserializing
			key = key.replaceAll("\"", "\\\\\"");
			value = value.replaceAll("\"", "\\\\\"");

			// Add the key and value between double quotes (since all elements are strings).
			str += "\"" + key + "\":\"" + value + "\",";
		}

		// Remove the last comma added
		if (str.endsWith(","))
			str = str.substring(0, str.length() - 1);

		// Close the map and return
		str += "}";
		return str;
	}
	

	/**
	 * Deserializes a {@code Map} from a {@code String}.
	 *
	 * @param str  the string to deserialize.
	 *
	 * @return the deserialized map.
	 *
	 * @see serialize
	 */
	public static Map<String, String> deserialize(String str) {
		Map<String, String> map = new HashMap<>();

		// Remove the leading and trailing curly brackets
		str = str.substring(1, str.length() - 1);

		// Add the map entrys, going through each character to ensure no mistakes in case
		// the elements of any key or value includes the " or , or : delimiters
		String key = "";
		String temp = "";
		char prev = 0x00;
		boolean inQuotes = false;
		for (char c : str.toCharArray()) {
			// Quotes begin or end if the character is a quote and the previous character was
			// not the \ to indicate this quote is a literal.
			if (c == '"' && prev != '\\')
				inQuotes = !inQuotes;
			else if (c == ':' && !inQuotes) {
				key = temp;
				temp = "";
			}
			else if (c == ',' && !inQuotes) {
				// Undo the quote \" literal operation from the serialize method
				key = key.replaceAll("\\\\\"", "\"");
				temp = temp.replaceAll("\\\\\"", "\"");
				
				map.put(key, temp);
				key = "";
				temp = "";
			}
			else
				temp += c;
			
			prev = c;
		}

		// Add the final entry created
		if (key.length() > 0) {
			key = key.replaceAll("\\\\\"", "\"");
			temp = temp.replaceAll("\\\\\"", "\"");
			map.put(key, temp);
		}

		// Return the completed map
		return map;
	}

}
