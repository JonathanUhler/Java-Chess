// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// JSON.java
// Chess
//
// Created by Jonathan Uhler on 3/31/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
//
// A custom JSON handler
//
public class JSONUtility {

    // ====================================================================================================
    // public static HashMap stringToDictionary
    //
    // The primary method of the JSON class. Converts a string to a dictionary
    //
    // Arguments--
    //
    // JSONString:  the string to convert
    //
    // Returns--
    //
    // JSONDict:    the converted dictionary
    //
    public static HashMap<String, String> stringToDictionary(String JSONString) {
        HashMap<String, String> JSONDict = new HashMap<>(); // Create a hashmap to store the kv pairs in

        if (JSONString.charAt(0) == '{') { JSONString = removeCharAt(JSONString, 0); }
        if (JSONString.charAt(JSONString.length() - 1) == '}') { JSONString = removeCharAt(JSONString, JSONString.length() - 1); } // Remove extra characters
        String[] kvPairs = JSONString.split(","); // Create a list of all the key:value pairs

        // For each kv pair
        for (String kv : kvPairs) {
            String[] parts = kv.split("="); // Separate the key and value

            // Clean up the key and value
            String key = parts[0].trim();
            String value = parts[1].trim();

            // Add the key and value to the dictionary
            JSONDict.put(key, value);
        }

        return JSONDict; // Return the dictionary
    }
    // end: public static HashMap stringToDictionary


    // ====================================================================================================
    // public static void write
    //
    // Writes data to a file
    //
    // Arguments--
    //
    // file:    the directory to write to
    //
    // date:    the data to write to the specified file
    //
    // Returns--
    //
    // None
    //
    public static void write(String file, String data) {
        try {
            Files.write(Paths.get(file), data.getBytes());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    // end: public static void write


    // ====================================================================================================
    // public static List<String> read
    //
    // Reads a file and returns its contents as a string array
    //
    // Arguments--
    //
    // file:    the file to read
    //
    // Returns--
    //
    // data:    the data as a string from the file
    //
    public static String read(String file) throws IOException {
        List<String> data = Files.readAllLines(Paths.get(file));
        return data.get(0);
    }
    // end: public static List<String> read


    // ====================================================================================================
    // public String removeCharAt
    //
    // Removes a character from a certain position in a string
    //
    // Arguments--
    //
    // str:             the string to edit
    //
    // n:               the index of the character to be removed
    //
    // Returns--
    //
    // front + back:    the new string
    private static String removeCharAt(String str, Integer n) {
        String front = str.substring(0, n);
        String back = str.substring(n + 1, str.length());
        return front + back;
    }
    // end: public String removeCharAt

}
// end: public class JSON