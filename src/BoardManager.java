// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// BoardManager.java
// Chess
//
// Created by Jonathan Uhler on 4/13/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class BoardManager
//
// Contains useful methods for the board
//
public class BoardManager {

    // ====================================================================================================
    // public static void playSound
    //
    // Plays a sound given a sound file
    //
    // Arguments--
    //
    // soundFile:   the path to the sound file (must be in .wav format)
    //
    // Returns--
    //
    // None
    //
    public static void playSound(String soundFile) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        File f = new File(soundFile);
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
    }
    // end: public static void playSound


    // ====================================================================================================
    // public static Point frameRelativeMousePosition
    //
    // Takes in the position of a JFrame and the mouse position and returns the mouse position relative
    // to the JFrame instead of relative to the entire computer screen.
    //
    // Arguments--
    //
    // frame:                   the JFrame the position should be based on
    //
    // screenRelativeMousePos:  the mouse position relative to the entire screen
    //
    // Returns--
    //
    // frameRelativeMousePos:   the mouse position relative to the JFrame frame
    //
    public static Point frameRelativeMousePosition(JFrame frame, Point screenRelativeMousePos) {
        Point frameRelativeMousePos = new Point();

        frameRelativeMousePos.x = screenRelativeMousePos.x - frame.getLocationOnScreen().x;
        frameRelativeMousePos.y = screenRelativeMousePos.y - frame.getLocationOnScreen().y;

        return frameRelativeMousePos;
    }
    // end: public static void frameRelativeMousePosition

}
// end: public class BoardManager
