// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Settings.java
// Chess
//
// Created by Jonathan Uhler on 4/4/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package graphics;


import main.Chess;
import util.FenUtility;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Settings
//
// Displays settings onto the screen
//
public class Settings {

    private static final int w = Chess.getGraphics().w;
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

        settingsPanel.add(newGame()); // Add the button to start a new game
        settingsPanel.add(showLegalMoves()); // Add the button to toggle legal moves
        settingsPanel.add(aiPlayer()); // Add the button to toggle the ai player
        settingsPanel.add(pieceMaterial()); // Add the text for the piece material
        settingsPanel.add(customFenPosition()); // Add the text box for the current fen position

        return settingsPanel;
    }
    // end: public static void drawSettings


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
            Chess.getBoard().loadPosition(customFenBox.getText());
            Chess.getGraphics().drawPosition();
            Chess.getGraphics().drawBoard(null);
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
        toggleLegalMoves.setForeground((Chess.getBoard().getShowLegalMoves()) ? new Color(125, 182, 107) : new Color(182, 103, 103));

        // Create an action listener for the button
        ActionListener legalMovesChanged = e -> {
            toggleLegalMoves.setForeground((!Chess.getBoard().getShowLegalMoves()) ? new Color(125, 182, 107) : new Color(182, 103, 103));
            Chess.getBoard().setShowLegalMoves(!Chess.getBoard().getShowLegalMoves());
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
        toggleAIPlayer.setForeground((Chess.getBoard().getEnableAI()) ? new Color(125, 182, 107) : new Color(182, 103, 103));

        // Create an action listener for the button
        ActionListener aiPlayerChanged = e -> {
            toggleAIPlayer.setForeground((!Chess.getBoard().getEnableAI()) ? new Color(125, 182, 107) : new Color(182, 103, 103));
            Chess.getBoard().setEnableAI(!Chess.getBoard().getEnableAI());
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
            Chess.getBoard().setThreeFoldRepetition(new HashMap<>());
            Chess.getBoard().loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0");
            Chess.getGraphics().drawPosition();
            Chess.getGraphics().drawBoard(null);
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
        int white = Chess.getBoard().countMaterial(Chess.getBoard().whiteIndex);
        int black = Chess.getBoard().countMaterial(Chess.getBoard().blackIndex);

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
