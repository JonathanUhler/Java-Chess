// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// Log.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package util;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class Log
//
// Provides logging and output utilities
//
public class Log {

	private static final String STDLOG_FILE = "Chess.log";


	public static final int DEBUG = 0;
	public static final int INFO = 1;
	public static final int WARN = 2;
	public static final int ERROR = 3;
	public static final int FATAL = 4;
	public static final String[] levelToString = {"DEBUG", "INFO", "WARN", "ERROR", "FATAL"};


	// ====================================================================================================
	// public static void gfxmsg
	//
	// Displays a graphical message
	//
	// Arguments--
	//
	//  title:   the title of the message window
	//
	//  message: the message to display
	//
	public static void gfxmsg(String title, Object message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
	}
	// end: public static void gfxmsg
	

	// ====================================================================================================
	// public static void stdout
	//
	// Prints to the standard output
	//
	// Arguments--
	//
	//  level:    the level of the message (info, debug, warn, error, fatal). If the level is unknown, it
	//            will be replaced by "Log.stdout"
	//
	//  location: the location the message originated from, such as a class or method name
	//
	//  message:  the message to print
	//
	public static void stdout(int level, String location, String message) {
		// Check if the level is known. If not, replace where the level string would be with "Log.stdout"
		if (level < Log.DEBUG || level > Log.FATAL)
			System.out.println("Log.stdout (" + location + ")  " + message);
		else
			System.out.println(levelToString[level] + " (" + location + ")  " + message);
	}
	// end: public static void stdout


	// ====================================================================================================
	// public static void stdlog
	//
	// Writes a message to a standard log file, and optionally prints it to the standard output
	//
	// Arguments--
	//
	//  level:    the level of the message (info, debug, warn, error, fatal). If the level is unknown, it
	//            will be replaced by "Log.stdout"
	//
	//  location: the location the message originated from, such as a class or method name
	//
	//  message:  the message to print
	//
	//  print:    an optional parameter to force the message to print (or not) to the standard output. By
	//            default, the message is only printed if it is WARN or higher
	//
	public static void stdlog(int level, String location, String message) {
		boolean print = (level == Log.WARN || level == Log.ERROR || level == Log.FATAL);
		Log.stdlog(level, location, message, print);
	}

	public static void stdlog(int level, String location, String message, boolean print) {
		// Print message to stdout if requested
		if (print)
			Log.stdout(level, location, message);

		// Write to the log file
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(Log.STDLOG_FILE),
															 StandardCharsets.UTF_8,
															 StandardOpenOption.APPEND,
															 StandardOpenOption.CREATE)) {
			if (level < Log.DEBUG || level > Log.FATAL)
				writer.write("Log.stdout (" + location + ")  " + message + "\n");
			else
				writer.write(levelToString[level] + " (" + location + ")  " + message + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// If the message was a fatal error, assume there is no way to recover and close to program to
		// prevent further errors or damage
		if (level == Log.FATAL)
			System.exit(Log.FATAL);
	}
	// end: public static void stdlog
	
}
// end: public class Log
