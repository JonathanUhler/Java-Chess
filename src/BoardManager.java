// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// BoardManager.java
// Chess
//
// Created by Jonathan Uhler on 4/13/21
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


import javax.sound.sampled.*;
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

}
// end: public class BoardManager
