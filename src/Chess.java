// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Chess.java
// Chess
//
// Created by Jonathan Uhler on 3/27/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Chess
//
// Main class of the project
//
public class Chess {

    // Create classes
    public static Graphics graphics;
    public static Board board;


    // ====================================================================================================
    // public static void main
    //
    // The main method. Initializes the board and the starting position
    //
    public static void main(String[] args) throws IOException {
        // Load all config for the user
        String theme, startingFEN, aiEnable;
        try {
            String stringifiedConfigData = JSONUtility.read(new File("./").getAbsoluteFile().getParentFile().getParentFile() + "/config/config.json"); // Get user config data
            HashMap<String, String> userConfigData = JSONUtility.stringToDictionary(stringifiedConfigData); // Convert user config data into a hashmap
            startingFEN = userConfigData.get("startingFEN"); // Get the theme and preference for starting color
            aiEnable = userConfigData.get("aiEnable");
        }
        catch (NoSuchFileException | IndexOutOfBoundsException e) {
            theme = "Gray";
            startingFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0";
            aiEnable = "false";
            JSONUtility.write(new File("./").getAbsoluteFile().getParentFile().getParentFile() + "/config/config.json", "{theme=" + theme + ", startingFEN=" + startingFEN + ", aiEnable=" + aiEnable + "}");
        }

        // Initialize classes
        graphics = new Graphics();
        board = new Board();

        // Initialize the board and starting position of the pieces
        graphics.createApplication(); // Create the JFrame window
        board.loadPosition(startingFEN); // Load the starting position
        board.enableAI = (aiEnable.equals("true"));
        JPanel settings = Settings.drawSettings(); // Display options
        graphics.drawPosition(); // Display all the pieces of the current position onto the frame
        graphics.drawBoard(null); // Display the tiles that make up the board
        graphics.appWindow.add(settings);
    }
    // end: public static void main
}
// end: public class Chess
