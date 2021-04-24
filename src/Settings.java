// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Settings.java
// Chess
//
// Created by Jonathan Uhler on 4/4/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Settings
//
// Displays settings onto the screen
//
public class Settings {

    private static final int w = Board.w;
    private static final JPanel settingsPanel = new JPanel();

    private static boolean perspectiveChanged;


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
        settingsPanel.setBounds(0, 0, Board.w, 60);

//        settingsPanel.add(playerColor()); // Add this menu to the view
        settingsPanel.add(newGame()); // Add the button to start a new game
        settingsPanel.add(boardTheme()); // Add the dropdown menu to the view
//        settingsPanel.add(changePerspective()); // Add the button to change player perspective
        settingsPanel.add(showLegalMoves()); // Add the button to toggle legal moves
        settingsPanel.add(customFenPosition()); // Add the text box for the current fen position

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
            HashMap<String, String> userConfigData = null; // Convert user config data into a hashmap
            try { userConfigData = JSONUtility.stringToDictionary(JSONUtility.read(Board.chessProjectPath + "/config/config.json"));
            } catch (IOException ioException) { ioException.printStackTrace(); }

            if (playerColor.getSelectedItem() == "White") { // If the user selected white, load the white FEN starting position
                assert userConfigData != null;
                userConfigData.put("startingFEN", "RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr w KQkq - 0 1"); // Save the starting FEN
                Board.loadPosition("RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr w KQkq - 0 1");
                Board.whitesMove = true;
            }
            else { // If the user selected black, load the black FEN starting position
                assert userConfigData != null;
                userConfigData.put("startingFEN", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"); // Save the starting FEN
                Board.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
                Board.whitesMove = false;
            }

            JSONUtility.write(Board.chessProjectPath + "/config/config.json", userConfigData.toString()); // Save the new data to the config file
            drawSettings();
            Board.drawPosition(); // Update the position
            Board.drawBoard(null);
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
        try { currentTheme = JSONUtility.stringToDictionary(JSONUtility.read(Board.chessProjectPath + "/config/config.json")).get("theme");
        } catch (IOException ioException) { ioException.printStackTrace(); }

        // Create dropdown menu for the board theme
        JComboBox<String> boardThemesDropdown = new JComboBox<>(new String[]{"Gray", "Green", "Blue", "Brown"});
        boardThemesDropdown.setSelectedItem(currentTheme);

        // Create an action listener for the theme dropdown menu
        ActionListener boardThemeChanged = e -> {
            HashMap<String, String> userConfigData = null; // Convert user config data into a hashmap
            try { userConfigData = JSONUtility.stringToDictionary(JSONUtility.read(Board.chessProjectPath + "/config/config.json"));
            } catch (IOException ioException) { ioException.printStackTrace(); }

            assert userConfigData != null;
            userConfigData.put("theme", (String) boardThemesDropdown.getSelectedItem()); // Save the new data

            JSONUtility.write(Board.chessProjectPath + "/config/config.json", userConfigData.toString()); // Save the new data
            drawSettings();
            Board.drawPosition(); // Update the position as well
            Board.drawBoard(null); // When the user changes the theme, update the board
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
        String fenToDisplay = FenUtility.changePlayerPerspective(FenUtility.buildFenFromPosition());

        JTextField customFenBox = new JTextField(fenToDisplay);

        // Create an action listener for the fen text field
        ActionListener fenPositionChanged = e -> {
            Board.loadPosition(customFenBox.getText());
            Board.drawPosition();
            Board.drawBoard(null);
        };

        customFenBox.addActionListener(fenPositionChanged); // Add the action listener to the text field
        customFenBox.setBounds((int) (w * 9.5), w * 3, w * 3, w); // Set the location of the text field

        return customFenBox;
    }
    // end: private static JTextField customFenPosition


    // ====================================================================================================
    // private static JButton changePerspective
    //
    // Changes the perspective of the game (which player is being viewed). Note, the perspective also
    // changes after each move automatically
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // changePerspective:   the button to change perspectives
    //
    private static JButton changePerspective() {
        JButton changePerspective = new JButton("Change View");

        changePerspective.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                perspectiveChanged = true;
                Board.whiteOnBottom = !Board.whiteOnBottom;
                Board.loadPosition(FenUtility.changePlayerPerspective(FenUtility.buildFenFromPosition()));
                Board.drawPosition();
                Board.drawBoard(null);
                drawSettings();
            }
        });
        changePerspective.setBounds((int) (w * 9.5), w * 3, w * 3, w); // Set the location of the button

        return changePerspective;
    }
    // end: private static JButton changePerspective


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
        JButton toggleLegalMoves = new JButton("Toggle Legal Moves");

        // Create an action listener for the button
        ActionListener legalMovesChanged = e -> Board.showLegalMoves = !Board.showLegalMoves;

        toggleLegalMoves.addActionListener(legalMovesChanged); // Add the action listener to the button
        toggleLegalMoves.setBounds((int) (w * 9.5), w * 3, w * 3, w); // Set the location of the button

        return toggleLegalMoves;
    }
    // end: private static JButton showLegalMoves


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
            Board.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0");
            Board.drawPosition();
            Board.drawBoard(null);
            drawSettings();
        };

        newGame.addActionListener(newGameRequested); // Add the action listener to the button
        newGame.setBounds((int) (w * 9.5), w * 3, w * 3, w); // Set the location of the button

        return newGame;
    }
    // end: private static JButton newGame

}
// end: public class Settings
