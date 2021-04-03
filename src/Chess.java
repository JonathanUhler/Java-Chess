// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Chess.java
// Chess
//
// Created by Jonathan Uhler on 3/27/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import java.io.File;
import java.io.IOException;
import java.util.HashMap;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Chess
//
// Main class of the project
//
public class Chess {

    // ====================================================================================================
    // public static void main
    //
    // The main method. Initialized the board and the starting position
    //
    public static void main(String[] args) throws IOException {
        // Load all config for the user
        String theme, startingFEN;
        try {
            String stringifiedConfigData = JSON.read(new File("./").getAbsoluteFile().getParentFile().getParentFile() + "/config/config.json"); // Get user config data
            HashMap<String, String> userConfigData = JSON.stringToDictionary(stringifiedConfigData); // Convert user config data into a hashmap
            theme = userConfigData.get("theme");
            startingFEN = userConfigData.get("startingFEN"); // Get the theme and preference for starting color
        }
        catch (IndexOutOfBoundsException indexException) {
            theme = "Classic";
            startingFEN = "RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr w KQkq - 0 1";
            JSON.write(new File("./").getAbsoluteFile().getParentFile().getParentFile() + "/config/config.json", "{theme=Blue, startingFEN=RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr w KQkq - 0 1}");
        }

        // Initialize the board and starting position of the pieces
        Board.createApplication(); // Create the JFrame window
        Board.drawSettings(); // Display all settings and config onto the window
        Board.drawBoard(theme); // Display the tiles that make up the board
        Board.loadPosition(startingFEN); // Load the starting position
        Board.drawPosition(); // Display all the pieces of the current position onto the frame
    }
    // end: public static void main
}
// end: public class Chess
