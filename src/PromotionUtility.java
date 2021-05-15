// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// PromotionUtility.java
// Chess
//
// Created by Jonathan Uhler on 4/24/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Objects;

// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class PromotionUtility
//
// Handles details of promotions
//
public class PromotionUtility {

    final JFrame promotionWindow = new JFrame(); // Create a new application window
    final JDialog promotionDialog = new JDialog(promotionWindow, "Promotion", true);
    private int promotionFlag = Move.Flag.none;


    // ====================================================================================================
    // public int getPromotionPiece
    //
    // Returns the piece chosen for promotion
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // promoteTo:   the piece to promote to
    //
    public int getPromotionPiece() {
        return promotionFlag;
    }
    // end: public int getPromotionPiece


    // ====================================================================================================
    // public JComboBox addPromotionMenu
    //
    // Creates a JComboBox to select the piece to promote to
    //
    // Arguments--
    //
    // None
    //
    // Returns--
    //
    // None
    //
    public JComboBox<String> addPromotionMenu() {
        JComboBox<String> promotionType = new JComboBox<>(new String[]{"Queen", "Rook", "Knight", "Bishop"});

        // Create an action listener for the theme dropdown menu
        ActionListener promotionChosen = e -> {
            String choice = (String) promotionType.getSelectedItem();

            switch (Objects.requireNonNull(choice)) {
                case "Queen":
                    promotionFlag = Move.Flag.promoteToQueen;
                    break;
                case "Rook":
                    promotionFlag = Move.Flag.promoteToRook;
                    break;
                case "Knight":
                    promotionFlag = Move.Flag.promoteToKnight;
                    break;
                case "Bishop":
                    promotionFlag = Move.Flag.promoteToBishop;
                    break;
            }

            promotionDialog.dispose();
        };

        promotionType.addActionListener(promotionChosen);
        promotionType.setBounds(0, 0, 150, 100);

        return promotionType;
    }
    // end: public JComboBox addPromotionMenu


    // ====================================================================================================
    // public void createPromotionWindow
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
    public void createPromotionWindow() {
        promotionDialog.setLayout(new FlowLayout());
        promotionDialog.add(addPromotionMenu());
        promotionDialog.setBounds(Chess.graphics.getWindowPosition().x + 275, Chess.graphics.getWindowPosition().y + 325, 200, 100);
        promotionDialog.setVisible(true);
        promotionWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    // end: public void createPromotionWindow

}
// end: public class PromotionUtility
