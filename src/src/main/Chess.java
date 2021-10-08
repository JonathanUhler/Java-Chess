// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Chess.java
// Chess
//
// Created by Jonathan Uhler on 3/27/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package main;


import board.Board;
import graphics.GraphicsHelper;
import graphics.Settings;
import javax.swing.*;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Chess
//
// Main class of the project
//
public class Chess {

    // Create classes
    private static GraphicsHelper graphics = new GraphicsHelper();
    private static Board board = new Board();


    // ====================================================================================================
    // GET methods
    public static GraphicsHelper getGraphics() {
        return graphics;
    }

    public static Board getBoard() {
        return board;
    }
    // end: GET methods


    // ====================================================================================================
    // public static void main
    //
    // The main method. Initializes the board and the starting position
    //
    public static void main(String[] args) {
        // Initialize the board and starting position of the pieces
        graphics.createApplication(); // Create the JFrame window
        board.loadPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0"); // Load the starting position
        JPanel settings = Settings.drawSettings(); // Display options
        graphics.drawPosition(); // Display all the pieces of the current position onto the frame
        graphics.drawBoard(null); // Display the tiles that make up the board
        graphics.appWindow.add(settings);
    }
    // end: public static void main
}
// end: public class Chess
