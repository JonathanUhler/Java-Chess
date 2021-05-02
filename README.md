# Java-Chess

A simple Chess game written in Java using the Swing graphics library.


# Dependencies

JDK 14 or higher - https://jdk.java.net/


# Compiling and Running

To run the project first navigate to the Java-Chess/src directory in a terminal or command line. The "Chess.class" file should already be in this directory. If it is not, compile the project using:

```
javac Chess.java
```

Once compiled (make sure the "Chess.class" file exists in your working directory), enter:
```
java Chess
```


# Loading and Saving Games

The project currently only supports FEN (forsyth-edwards notation) for saving and loading games. When playing, a JTextField at the top of the program will update after each move to properly display the board state. FEN positions can also be loaded by pasting or writing a valid position into the text field and pressing the 'enter' key.
