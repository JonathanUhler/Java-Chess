// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// BoardManager.java
// Chess
//
// Created by Jonathan Uhler on 4/13/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package board;


import move.Move;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;


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
    // public static void debugMessage
    //
    // Prints a debug message from a standard location
    //
    // Arguments--
    //
    // msg: the message to print
    //
    // Returns--
    //
    // None
    //
    public static void debugMessage(String msg) {
        System.out.println("BoardManager.debugMessage % " + msg);
    }
    // end: public static void debugMessage


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


    // ====================================================================================================
    // public static HashMap<Move, Integer> sortByValue
    //
    // Sorts a hashmap by its values
    //
    // Arguments--
    //
    // hm:      the hashmap to sort
    //
    // Returns--
    //
    // temp:    the sorted hashmap
    public static HashMap<Move, Integer> sortByValue(HashMap<Move, Integer> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<Move, Integer> > list = new LinkedList<>(hm.entrySet());

        // Sort the list
        list.sort(Map.Entry.comparingByValue());

        // put data from sorted list to hashmap
        HashMap<Move, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<Move, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }

        return temp;
    }
    // end: public static HashMap<Move, Integer> sortByValue


    // ====================================================================================================
    // public static HashMap<String, Integer> copy
    //
    // Returns a deep copy of a hashmap
    //
    // Arguments--
    //
    // original:    the original hashmap to copy
    //
    // Returns--
    //
    // copy:        the hashmap copy
    //
    public static HashMap<String, Integer> copy(HashMap<String, Integer> original) {
        HashMap<String, Integer> copy = new HashMap<>();
        for (Map.Entry<String, Integer> entry : original.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }
    // end: public static HashMap<String, Integer> copy

}
// end: public class BoardManager
