// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Chess.java
// Chess
//
// Created by Jonathan Uhler on 3/27/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Chess
//
// Main class of the project
//
public class Chess {

    // ====================================================================================================
    // public static void main
    //
    // The main method
    //
    public static void main(String[] args) {
        // MARK: here, the user's theme (for when calling drawBoard() needs to be loaded from the config file)
        Board.createApplication(); // Create the JFrame window
        Board.drawBoard("Classic"); // Display the tiles that make up the board
        Board.loadPosition(FenUtility.startFen); // Load the starting position
        Board.drawPosition(); // Display all the pieces of the current position onto the frame
    }
    // end: public static void main
}
// end: public class Chess
