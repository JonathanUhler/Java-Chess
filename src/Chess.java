// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Chess.java
// Chess
//
// Created by Jonathan Uhler on 3/27/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import javax.swing.*;
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
    // The main method. Initializes the board and the starting position
    //
    public static void main(String[] args) throws IOException {
        // Load all config for the user
        String theme, startingFEN;
        try {
            String stringifiedConfigData = JSONUtility.read(new File("./").getAbsoluteFile().getParentFile().getParentFile() + "/config/config.json"); // Get user config data
            HashMap<String, String> userConfigData = JSONUtility.stringToDictionary(stringifiedConfigData); // Convert user config data into a hashmap
            startingFEN = userConfigData.get("startingFEN"); // Get the theme and preference for starting color
        }
        catch (IndexOutOfBoundsException indexException) {
            theme = "Gray";
            startingFEN = "pppppppp/rnbqkbnr/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 1 0";
            JSONUtility.write(new File("./").getAbsoluteFile().getParentFile().getParentFile() + "/config/config.json", "{theme=" + theme + ", startingFEN=" + startingFEN + "}");
        }

        // Initialize the board and starting position of the pieces
        Board.createApplication(); // Create the JFrame window

        Board.loadPosition(startingFEN); // Load the starting position

        JPanel settings = Settings.drawSettings(); // Display options

        Board.drawPosition(); // Display all the pieces of the current position onto the frame

        Board.drawBoard(null); // Display the tiles that make up the board

        Board.appWindow.add(settings);
    }
    // end: public static void main
}
// end: public class Chess
