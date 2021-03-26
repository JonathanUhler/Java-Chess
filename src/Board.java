// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Board.java
// Chess
//
// Created by Jonathan Uhler on 3/25/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import java.awt.*;
import javax.swing.JComponent;
import javax.swing.JFrame;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// class Tile
//
class Tiles extends JComponent {

    // Create variables for the size of the tiles and their starting position (starting = position of top-left tile)
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize(); // Get the resolution of the current screen
    int screenHeight = (int)size.getHeight(); // Break that down into the w/h components of the screen
    public int w = (int) ((screenHeight * 0.8) / 10), h = w, x = w, y = h;
    // Create variable for the color of a given tile
    public Color c = new Color(0, 0, 0);


    // ====================================================================================================
    // public void paintComponent
    //
    // Extends painComponent method from java.awt
    //
    // Arguments--
    //
    // g:   a Graphics element
    //
    // Returns--
    //
    // None
    //
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                // If the row plus the column is divisible by 2, then set it to one color
                if ((row + col) % 2 == 1) {
                    c = Color.BLACK;
                }
                // Otherwise, set it to the other color
                else {
                    c = Color.WHITE;
                }

                // Update the x/y position of each tile
                x = (col + 1) * w;
                y = (row + 1) * h;
                draw(g); // Draw the tile

            }
        }
    }
    // end: public void paintComponent


    // ====================================================================================================
    // public void draw
    //
    // Extends draw method from java.awt
    //
    // Arguments--
    //
    // g:   a Graphics element
    //
    // Returns--
    //
    // None
    //
    public void draw(Graphics g) {
        g.setColor(c); // Set the color of the tile
        g.fillRect(x, y, w, h); // Draw the tile
    }
    // end: public void draw

}
// end: class Tile


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Board
//
public class Board {

    // ====================================================================================================
    // public static void main
    //
    // Main method
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // None
    public static void main(String[] args) {

        Dimension size = Toolkit.getDefaultToolkit().getScreenSize(); // Get the resolution of the current screen
        int screenHeight = (int)size.getHeight(); // Break that down into the w/h components of the screen
        int w = (int) (screenHeight * 0.8), h = w;

        JFrame window = new JFrame(); // Create a new frame
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Terminate the frame when the 'x' button of the application window is pressed
        window.setBounds(30, 30, w, h); // Set the size of the frame
        window.setResizable(false); // Prevent the JFrame from being resized
        window.getContentPane().add(new Tiles()); // Add the tiles to the board
        window.setVisible(true); // Show the frame

    }
    // end: public static void main
}
// end: public class Board