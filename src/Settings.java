// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Settings.java
// Chess
//
// Created by Jonathan Uhler on 4/4/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Settings
//
// Displays settings onto the screen
//
public class Settings {

    private static final int w = Chess.graphics.w;
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
        settingsPanel.setBounds(0, 0, w, 60);

//        settingsPanel.add(playerColor()); // Add this menu to the view
        settingsPanel.add(newGame()); // Add the button to start a new game
        settingsPanel.add(boardTheme()); // Add the dropdown menu to the view
        settingsPanel.add(showLegalMoves()); // Add the button to toggle legal moves
        settingsPanel.add(aiPlayer()); // Add the button to toggle the ai player
        settingsPanel.add(pieceMaterial()); // Add the text for the piece material
        settingsPanel.add(customFenPosition()); // Add the text box for the current fen position

        return settingsPanel;
    }
    // end: public static void drawSettings


    // ====================================================================================================
    // private static JComboBox playerColor
    //
    // Creates the combo box for the players desired starting color
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
            HashMap<String, String> userConfigData = null; // Convert user config data into a hashmap
            try { userConfigData = JSONUtility.stringToDictionary(JSONUtility.read(Chess.board.chessProjectPath + "/config/config.json"));
            } catch (IOException ioException) { ioException.printStackTrace(); }

            if (playerColor.getSelectedItem() == "White") { // If the user selected white, load the white FEN starting position
                assert userConfigData != null;
                userConfigData.put("startingFEN", "RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr w KQkq - 0 1"); // Save the starting FEN
                Chess.board.loadPosition("RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr w KQkq - 0 1");
                Chess.board.whitesMove = true;
            }
            else { // If the user selected black, load the black FEN starting position
                assert userConfigData != null;
                userConfigData.put("startingFEN", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"); // Save the starting FEN
                Chess.board.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
                Chess.board.whitesMove = false;
            }

            JSONUtility.write(Chess.board.chessProjectPath + "/config/config.json", userConfigData.toString()); // Save the new data to the config file
            drawSettings();
            Chess.graphics.drawPosition(); // Update the position
            Chess.graphics.drawBoard(null);
        };

        playerColor.addActionListener(preferredSideChanged); // Add the action listener to the player color menu
        playerColor.setBounds((int) (w * 9.5), (int) (w * 0.8), w * 2, w); // Set the position of this menu

        return playerColor;
    }
    // end: public static JComboBox playerColor


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
        String currentTheme = ""; // Read the theme
        try { currentTheme = JSONUtility.stringToDictionary(JSONUtility.read(Chess.board.chessProjectPath + "/config/config.json")).get("theme");
        } catch (IOException ioException) { ioException.printStackTrace(); }

        // Create dropdown menu for the board theme
        JComboBox<String> boardThemesDropdown = new JComboBox<>(new String[]{"Gray", "Green", "Blue", "Brown"});
        boardThemesDropdown.setSelectedItem(currentTheme);

        // Create an action listener for the theme dropdown menu
        ActionListener boardThemeChanged = e -> {
            HashMap<String, String> userConfigData = null; // Convert user config data into a hashmap
            try { userConfigData = JSONUtility.stringToDictionary(JSONUtility.read(Chess.board.chessProjectPath + "/config/config.json"));
            } catch (IOException ioException) { ioException.printStackTrace(); }

            assert userConfigData != null;
            userConfigData.put("theme", (String) boardThemesDropdown.getSelectedItem()); // Save the new data

            JSONUtility.write(Chess.board.chessProjectPath + "/config/config.json", userConfigData.toString()); // Save the new data
            drawSettings();
            Chess.graphics.drawPosition(); // Update the position as well
            Chess.graphics.drawBoard(null); // When the user changes the theme, update the board
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
        String fenToDisplay = FenUtility.buildFenFromPosition();

        JTextField customFenBox = new JTextField(fenToDisplay);

        // Create an action listener for the fen text field
        ActionListener fenPositionChanged = e -> {
            Chess.board.loadPosition(customFenBox.getText());
            Chess.graphics.drawPosition();
            Chess.graphics.drawBoard(null);
        };

        customFenBox.addActionListener(fenPositionChanged); // Add the action listener to the text field
        customFenBox.setBounds((int) (w * 9.5), w * 3, w * 3, w); // Set the location of the text field

        return customFenBox;
    }
    // end: private static JTextField customFenPosition


    // ====================================================================================================
    // private static JButton showLegalMoves
    //
    // Toggles whether or not legal moves are shown
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // toggleLegalMoves:    the button that toggles legal moves
    //
    private static JButton showLegalMoves() {
        JButton toggleLegalMoves = new JButton("Show Legal Moves");
        toggleLegalMoves.setForeground((Chess.board.showLegalMoves) ? new Color(125, 182, 107) : new Color(182, 103, 103));

        // Create an action listener for the button
        ActionListener legalMovesChanged = e -> {
            toggleLegalMoves.setForeground((!Chess.board.showLegalMoves) ? new Color(125, 182, 107) : new Color(182, 103, 103));
            Chess.board.showLegalMoves = !Chess.board.showLegalMoves;
        };

        toggleLegalMoves.addActionListener(legalMovesChanged); // Add the action listener to the button
        toggleLegalMoves.setBounds((int) (w * 9.5), w * 3, w * 3, w); // Set the location of the button

        return toggleLegalMoves;
    }
    // end: private static JButton showLegalMoves


    // ====================================================================================================
    // private static JButton aiPlayer
    //
    // Toggles whether or not the ai player is enabled
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // toggleAIPlayer:  the button that toggles the ai
    //
    private static JButton aiPlayer() {
        JButton toggleAIPlayer = new JButton("Enable AI");
        toggleAIPlayer.setForeground((Chess.board.enableAI) ? new Color(125, 182, 107) : new Color(182, 103, 103));

        // Create an action listener for the button
        ActionListener aiPlayerChanged = e -> {
            toggleAIPlayer.setForeground((!Chess.board.enableAI) ? new Color(125, 182, 107) : new Color(182, 103, 103));
            Chess.board.enableAI = !Chess.board.enableAI;

            // Write the new config data
            HashMap<String, String> userConfigData = null; // Convert user config data into a hashmap
            try { userConfigData = JSONUtility.stringToDictionary(JSONUtility.read(Chess.board.chessProjectPath + "/config/config.json"));
            } catch (IOException ioException) { ioException.printStackTrace(); }

            assert userConfigData != null;
            userConfigData.put("aiEnable", String.valueOf(Chess.board.enableAI)); // Save the new data
            JSONUtility.write(Chess.board.chessProjectPath + "/config/config.json", userConfigData.toString()); // Save the new data
        };

        toggleAIPlayer.addActionListener(aiPlayerChanged); // Add the action listener to the button
        toggleAIPlayer.setBounds((int) (w * 9.5), w * 3, w * 3, w); // Set the location of the button

        return toggleAIPlayer;
    }
    // end: private static JButton aiPlayer


    // ====================================================================================================
    // private static JButton newGame
    //
    // Starts a new game
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // newGame:    the button that toggles legal moves
    //
    private static JButton newGame() {
        JButton newGame = new JButton("New Game");

        // Create an action listener for the button
        ActionListener newGameRequested = e -> {
            Chess.board.threeFoldRepetition = new HashMap<>();
            Chess.board.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0");
            Chess.graphics.drawPosition();
            Chess.graphics.drawBoard(null);
            drawSettings();
        };

        newGame.addActionListener(newGameRequested); // Add the action listener to the button
        newGame.setBounds((int) (w * 9.5), w * 3, w * 3, w); // Set the location of the button

        return newGame;
    }
    // end: private static JButton newGame


    // ====================================================================================================
    // private static JLabel pieceMaterial
    //
    // Shows piece advantage/score
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // material:    a label with the piece materials
    //
    private static JLabel pieceMaterial() {
        int white = Chess.board.countMaterial(Chess.board.whiteIndex);
        int black = Chess.board.countMaterial(Chess.board.blackIndex);

        String materialMessage;

        materialMessage = "Material: Black +" + (black - white);
        if (white > black) { materialMessage = "Material: White +" + (white - black); }
        else if (white == black) { materialMessage = "Material: Even +0"; }

        JLabel material = new JLabel(materialMessage);
        material.setBounds((int) (w * 9.5), w * 3, w * 3, w); // Set the location of the label

        return material;
    }
    // end: private static JLabel pieceMaterial

}
// end: public class Settings
