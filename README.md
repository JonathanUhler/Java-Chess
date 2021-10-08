# Java-Chess
A simple Chess game written in Java using the Swing graphics library.


# Dependencies
JDK 17 or above - https://www.oracle.com/java/technologies/downloads/


# Installation
*Note: The installation process will be expanded in the future, but currently only supports Mac as a native application*

## MacOS
### MacOS Installation
Clone or download the repository from Github.\
In Finder go to the Java-Chess/release folder.\
Double-click on the ```JavaChess-1.0.dmg``` to launch the installer. The ```JavaChess.app``` application should appear in a new window, drag this to the Applications folder or another location.

### MacOS Build Process
To build or update the app, follow the process below:
1) Open Terminal and ```cd``` to the Java-Chess/ directory
2) Compile with ```./compile.sh```
3) Make the jarfile with ```./jar.sh```
4) Build the app bundle with ```./build-mac.sh```
5) Follow the instructions under MacOS Installation to open and use the app

## Linux
*Note: The installation process will be expanded in the future, but currently only supports Mac as a native application*\
\
In the meantime, the app can still be used by running the jarfile directly.

### Linux Build Process
To build or update the app, follow the process below:
1) Open Terminal and ```cd``` to the Java-Chess/ directory
2) Compile with ```./compile.sh```
3) Make the jarfile with ```./jar.sh```
4) Run the app with ```java -jar src/Chess.jar```

## Windows
*Note: The installation process will be expanded in the future, but currently only supports Mac as a native application*\
\
In the meantime, the jarfile can still be run directly from the terminal/command line. However because Windows is not Unix-based, the shell scripts used for Mac and Linux will not work.


# Loading and Saving Games
The project currently only supports FEN (forsyth-edwards notation) for saving and loading games. When playing, a JTextField at the top of the program will update after each move to properly display the board state. FEN positions can also be loaded by pasting or writing a valid position into the text field and pressing the 'enter' key.


# AI Player
The project currently has an AI/computer player that can be enabled/disabled by pressing the "Enable AI" button. To play against the computer make sure the button is green, then make your first move; the computer will think for a few seconds and then play its move.

## Technical information about the computer player
Algorithm: minimax\
Speed optimizations: none of note, the search is currently pure\
Skill optimizations: capture incentives, piece tile tables (positional play incentives)
