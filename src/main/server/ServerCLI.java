// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// ServerCLI.java
// Networking-Chess
//
// Created by Jonathan Uhler
// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=


package server;


import util.Log;
import tests.PerftTest;
import engine.board.Board;
import engine.board.BoardInfo;
import engine.fen.FenUtility;
import java.util.ArrayList;
import java.util.Scanner;


// +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
// public class ServerCLI
//
// A simple CLI interface for server-side manipulation of the board state and info
//
public class ServerCLI {

	private Server server; // Server hosting the CLI, used to call get/set methods
	

	// ----------------------------------------------------------------------------------------------------
	// public ServerCLI
	//
	// Arguments--
	//
	//  server: the server hosting the CLI, used to call get/set methods to access board information
	//
	public ServerCLI(Server server) {
		this.server = server;
	}
	// end: public ServerCLI
	

	// ====================================================================================================
	// public void run
	//
	// Runs the CLI interface, must be called manually and is not invoked by the constructor so that
	// there is time for the server hosting the CLI to save the CLI as an object that can be compared
	// to the "sender" argument in get/set methods
	//
	public void run() {
		Scanner cliIn = new Scanner(System.in);
		Log.stdout(Log.INFO, "ServerCLI", "CLI established, type \"help\"");

		// Constantly read user input and pass to the process method
		while (cliIn.hasNext())
			this.process(cliIn.nextLine());
	}
	// end: public void run


	// ====================================================================================================
	// private ArrayList<String> getopt
	//
	// A simple routine to split an input string into options. Splits the argument input by spaces such that
	// the first element of the array is the command and subsequent elements are arguments to that command.
	// This method takes quoted string into account (allowing for arguments that contain spaces themselves)
	//
	// Arguments--
	//
	//  input: the input string to split
	//
	// Returns--
	//
	//  An ArrayList of strings, as specified above
	//
	private ArrayList<String> getopt(String input) {
		ArrayList<String> parsed = new ArrayList<>();

		String arg = "";
		boolean quoted = false;

		// Loop through each character to catch quoted string
		for (char c : input.toCharArray()) {
			// Update quote status
			if (c == '\"')
				quoted = !quoted;
			// Reached the end of an argument and not in a quoted string, so add that argument
			else if (c == ' ' && !quoted) {
				parsed.add(arg);
				arg = "";
			}
			// Add to the argument
			else
				arg += c;
		}
		// Since there is no space at the end of the input string, add the final argument being built
		parsed.add(arg);

		return parsed;
	}
	// end: private ArrayList<String> getopt


	// ====================================================================================================
	// private void process
	//
	// Processes an input string as a command, and calls the appropriate routine
	//
	// Arguments--
	//
	//  input: the input string for a command
	//
	private void process(String input) {
		// Get the options for input formatted
		ArrayList<String> args = this.getopt(input);
		if (args.size() == 0) {
			Log.stdout(Log.ERROR, "ServerCLI", "Invalid entry");
			return;
		}

		String cmd = args.remove(0); // Remove the first element (command name) and assign the value to this variable

		// Call routine
		switch (cmd) {
		case "help" -> this.help();
		case "set" -> this.set(args);
		case "get" -> this.get();
		case "reset" -> this.reset();
		case "perft" -> this.perft(args);
		case "addr" -> this.addr();
		default -> Log.stdout(Log.ERROR, "ServerCLI", "Invalid command: " + cmd);
		}
	}
	// and: private void process


	// ====================================================================================================
	// COMMAND methods
	private void help() {
		// Print all the help information
		Log.stdout(Log.INFO, "ServerCLI", "Usage: <command> [args]");
		Log.stdout(Log.INFO, "ServerCLI", "\tset <fen>");
		Log.stdout(Log.INFO, "ServerCLI", "\t\tsets the board to a given fen string");
		Log.stdout(Log.INFO, "ServerCLI", "\tget");
		Log.stdout(Log.INFO, "ServerCLI", "\t\tprints the current BoardInfo object");
		Log.stdout(Log.INFO, "ServerCLI", "\treset");
		Log.stdout(Log.INFO, "ServerCLI", "\t\tsets the boards to the starting position");
		Log.stdout(Log.INFO, "ServerCLI", "\tperft [-s <start>] [-e <end>] [-d]");
		Log.stdout(Log.INFO, "ServerCLI", "\t\truns perft test suite, optionally with a start/end test");
		Log.stdout(Log.INFO, "ServerCLI", "\t\tand the ability to print node count after each move");
		Log.stdout(Log.INFO, "ServerCLI", "\taddr");
		Log.stdout(Log.INFO, "ServerCLI", "\t\tprints the server address and port");
		Log.stdout(Log.INFO, "ServerCLI", "\thelp");
		Log.stdout(Log.INFO, "ServerCLI", "\t\tprint this message");
	}


	private void set(ArrayList<String> args) {
		if (args.size() == 0) {
			Log.stdout(Log.ERROR, "ServerCLI", "Missing argument for set <fen>");
			return;
		}

		String arg = args.get(0);
		BoardInfo setInfo = FenUtility.informationFromFen(arg);
		if (setInfo == null) {
			Log.stdout(Log.ERROR, "ServerCLI", "Invalid argument for set: " + arg);
			return;
		}
		Log.stdout(Log.INFO, "ServerCLI", "Updating board...");
		this.server.setBoardInfo(setInfo, this);
	}


	private void get() {
		BoardInfo boardInfo = this.server.getBoardInfo(this);
		if (boardInfo != null)
			Log.stdout(Log.INFO, "ServerCLI", boardInfo.toString());
		else
			Log.stdout(Log.ERROR, "ServerCLI", "Cannot get board information, null found");
	}


	private void reset() {
		ArrayList<String> args = new ArrayList<>();
		args.add(Board.START_FEN);
		this.set(args);
	}


	private void perft(ArrayList<String> args) {
		int start = 0;
		int end = PerftTest.TESTS.size() - 1;
		boolean divide = args.contains("-d");

		// Parse arguments for start/end test number
		try {
			int startIndex = args.indexOf("-s");
			if (startIndex != -1 && startIndex < args.size() - 1)
				start = Integer.parseInt(args.get(startIndex + 1));
			int endIndex = args.indexOf("-e");
			if (endIndex != -1 && endIndex < args.size() - 1)
				end = Integer.parseInt(args.get(endIndex + 1));
		}
		catch (NumberFormatException e) {
			Log.stdout(Log.ERROR, "ServerCLI", "Invalid argument for perft -s or -e, must be an integer");
			return;
		}
		
		Log.stdout(Log.INFO, "ServerCLI", "Running perft test suite, this may take several minutes...");
		PerftTest.run(start, end, divide);
	}


	private void addr() {
		Log.stdout(Log.INFO, "ServerCLI", "Server can be reached at: " +
				   this.server.getIP() + ":" + this.server.getPort());
	}
	// end: COMMAND methods
	
}
