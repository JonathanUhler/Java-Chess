// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// GameStateUtility.java
// Chess
//
// Created by Jonathan Uhler on 5/1/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package util;


import graphics.Settings;
import main.Chess;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class GameStateUtility
//
// Handles wins, draws, and other details of figuring out the current game-state
//
public class GameStateUtility {

    static final JFrame endWindow = new JFrame(); // Create a new application window
    static JDialog endDialog = new JDialog(endWindow, "Game Finished", true);


    // ====================================================================================================
    // public static int actOnGameState
    //
    // Acts on the state of the game
    //
    // Arguments--
    //
    // numLegalMoves:   the number of legal moves the player has
    //
    // inCheck:         whether or not the player is in check
    //
    // Returns--
    //
    // state of the game as an integer
    public static int actOnGameState(int numLegalMoves, boolean inCheck) {
        // 0 = ongoing game
        // 1 = draw by 50-move rule
        // 2 = draw by threefold repetition
        // 3 = draw by stalemate
        // 4 = win/loss by checkmate

        if (Chess.getBoard().getFiftyMoveRule() >= 50) {
            gameOver("Draw: 50-move rule");
            return 1; // Draw by 50-move rule
        }
        else if (Chess.getBoard().getThreeFoldRepetition().get(Chess.getBoard().getCurrentFenPosition()) != null && Chess.getBoard().getThreeFoldRepetition().get(Chess.getBoard().getCurrentFenPosition()) >= 3) {
            gameOver("Draw: Threefold repetition");
            return 2; // Draw by threefold repetition
        }
        else if (numLegalMoves == 0) {
            if (!inCheck) {
                gameOver("Draw: Stalemate");
                return 3; // Draw by stalemate
            }

            gameOver("Checkmate: " + (((Chess.getBoard().getColorToMove() ^ 1) == 0) ? "White" : "Black") + " wins");
            return 4; // Win by checkmate if no legal moves and not stalemate
        }
        else {
            return 0; // Game still ongoing
        }
    }
    // end: public static int actOnGameState


    // ====================================================================================================
    // static void gameOver
    //
    // Ends the game with a popup
    //
    // Arguments--
    //
    // endState:    a message to describe how the game ended
    //
    // Returns--
    //
    // None
    //
    static void gameOver(String endState) {
        endDialog = new JDialog(endWindow, "Game Finished", true);;
        endDialog.add(addEndText(endState));
        endDialog.add(addCloseButton());
        createEndWindow();
    }
    // end: static void gameOver


    // ====================================================================================================
    // static JLabel addEndText
    //
    // Creates a JLabel to explain how the game ended
    //
    // Arguments--
    //
    // text:    text to display in the label
    //
    // Returns--
    //
    // JLabel:  a new JLabel
    //
    static JLabel addEndText(String text) {
        return new JLabel(text);
    }
    // end: static JLabel addEndText


    // ====================================================================================================
    // static JButton addCloseButton
    //
    // Creates a JButton to close the popup
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // closeButton: the JButton to close the popup
    //
    static JButton addCloseButton() {
        JButton closeButton = new JButton("OK");

        ActionListener close = e -> {
            endDialog.dispose();

            Chess.getBoard().setThreeFoldRepetition(new HashMap<>());
            Chess.getBoard().loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0");
            Chess.getGraphics().drawPosition();
            Chess.getGraphics().drawBoard(null);
            Settings.drawSettings();
        };

        closeButton.addActionListener(close);

        return closeButton;
    }
    // end: static JButton addCloseButton


    // ====================================================================================================
    // public void createEndWindow
    //
    // Creates the JFrame
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // None
    //
    static void createEndWindow() {
        endDialog.setLayout(new FlowLayout());
        endDialog.setBounds(Chess.getGraphics().getWindowPosition().x + 275, Chess.getGraphics().getWindowPosition().y + 325, 200, 100);
        endDialog.setVisible(true);
        endWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    // end: public void createEndWindow

}
// end: public class GameStateUtility
