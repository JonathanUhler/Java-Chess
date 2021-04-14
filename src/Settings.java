// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Settings.java
// Chess
//
// Created by Jonathan Uhler on 4/4/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Settings
//
// Displays settings onto the screen
//
public class Settings {

    private static final int w = Board.w;
    private static final JPanel settingsPanel = new JPanel();


    // ====================================================================================================
    // public static JPanel drawSettings
    //
    // Compiles the different settings and options and returns them as a JPanel
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // settingsPanel:   a JPanel containing the settings and options for the game
    //
    public static JPanel drawSettings() {

        settingsPanel.removeAll();

        settingsPanel.add(playerColor()); // Add this menu to the view
        settingsPanel.add(boardTheme()); // Add the dropdown menu to the view
        settingsPanel.add(customFenPosition());

        return settingsPanel;
    }
    // end: public static void drawSettings


    // ====================================================================================================
    // private static JComboBox playerColor
    //
    // Creates the combo box for the players desired stating color
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // playerColor:     the combo box for the player color choices
    public static JComboBox<String> playerColor() {
        // Create dropdown menu for the preferred piece color
        JComboBox<String> playerColor = new JComboBox<>(new String[]{"White", "Black"});
        // Create an action listener for the preferred piece color
        ActionListener preferredSideChanged = e -> {
            String stringifiedConfigData = null; // Get user config data
            try { stringifiedConfigData = JSONUtility.read(new File("./").getAbsoluteFile().getParentFile().getParentFile() + "/config/config.json"); } catch (IOException ioException) { ioException.printStackTrace(); }
            assert stringifiedConfigData != null;
            HashMap<String, String> userConfigData = JSONUtility.stringToDictionary(stringifiedConfigData); // Convert user config data into a hashmap

            if (playerColor.getSelectedItem() == "White") { // If the user selected white, load the white FEN starting position
                userConfigData.put("startingFEN", "RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr w KQkq - 0 1"); // Save the starting FEN
                Board.loadPosition("RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr w KQkq - 0 1");
                Board.whitesMove = true;
            }
            else { // If the user selected black, load the black FEN starting position
                userConfigData.put("startingFEN", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"); // Save the starting FEN
                Board.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
                Board.whitesMove = false;
            }

            JSONUtility.write(Board.chessProjectPath + "/config/config.json", userConfigData.toString()); // Save the new data to the config file
            drawSettings();
            Board.drawPosition(); // Update the position
            Board.drawBoard(userConfigData.get("theme"));
        };

        playerColor.addActionListener(preferredSideChanged); // Add the action listener to the player color menu
        playerColor.setBounds((int) (w * 9.5), (int) (w * 0.8), w * 2, w); // Set the position of this menu

        return playerColor;
    }


    // ====================================================================================================
    // private static JComboBox boardTheme
    //
    // Creates the combo box for the board theme
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // boardThemesDropdown:  the JComboBox for the board theme
    //
    public static JComboBox<String> boardTheme() {
        // Create dropdown menu for the board theme
        JComboBox<String> boardThemesDropdown = new JComboBox<>(new String[]{"Classic", "Green", "Blue", "Brown"});
        // Create an action listener for the theme dropdown menu
        ActionListener boardThemeChanged = e -> {
            String stringifiedConfigData = null; // Get user config data
            try { stringifiedConfigData = JSONUtility.read(new File("./").getAbsoluteFile().getParentFile().getParentFile() + "/config/config.json"); } catch (IOException ioException) { ioException.printStackTrace(); }
            assert stringifiedConfigData != null;
            HashMap<String, String> userConfigData = JSONUtility.stringToDictionary(stringifiedConfigData); // Convert user config data into a hashmap

            userConfigData.put("theme", (String) boardThemesDropdown.getSelectedItem()); // Save the new data

            JSONUtility.write(Board.chessProjectPath + "/config/config.json", userConfigData.toString()); // Save the new data
            drawSettings();
            Board.drawPosition(); // Update the position as well
            Board.drawBoard((String) Objects.requireNonNull(boardThemesDropdown.getSelectedItem())); // When the user changes the theme, update the board
        };

        boardThemesDropdown.addActionListener(boardThemeChanged); // Add the action listener to the dropdown menu
        boardThemesDropdown.setBounds((int) (w * 9.5), (int) (w * 1.5), w * 3, w); // Set the location of the dropdown menu

        return boardThemesDropdown;
    }
    // end: private static JComboBox boardTheme


    // ====================================================================================================
    // private static JTextField customFenPosition
    //
    // Creates a text field for a custom fen position
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // customFenBox:    the JTextField for the custom fen position
    //
    private static JTextField customFenPosition() {
        String stringifiedConfigData = null; // Get user config data
        try { stringifiedConfigData = JSONUtility.read(new File("./").getAbsoluteFile().getParentFile().getParentFile() + "/config/config.json"); } catch (IOException ioException) { ioException.printStackTrace(); }
        assert stringifiedConfigData != null;
        HashMap<String, String> userConfigData = JSONUtility.stringToDictionary(stringifiedConfigData); // Convert user config data into a hashmap
        JTextField customFenBox = new JTextField(FenUtility.buildFenFromPosition());

        // Create an action listener for the fen text field
        ActionListener fenPositionChanged = e -> {
            Board.loadPosition(customFenBox.getText());
            Board.drawPosition();
            Board.drawBoard(userConfigData.get("theme"));
        };

        customFenBox.addActionListener(fenPositionChanged); // Add the action listener to the text field
        customFenBox.setBounds((int) (w * 9.5), w * 3, w * 3, w); // Set the location of the text field

        return customFenBox;
    }
    // end: private static JTextField customFenPosition

}
// end: public class Settings
