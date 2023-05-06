package server;


import jnet.Log;
import tests.PerftTest;
import engine.board.Board;
import engine.board.BoardInfo;
import engine.fen.FenUtility;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;


/**
 * A simple CLI interface for server-side manipulation of the board state and information.
 *
 * @author Jonathan Uhler
 */
public class ServerCLI {

	/** The server hosting this CLI. */
	private Server server;
	

	/**
	 * Constructs a new {@code ServerCLI} object.
	 *
	 * @param server  the server hosting this CLI.
	 */
	public ServerCLI(Server server) {
		this.server = server;
	}
	

	/**
	 * Runs the command line interface on the current thread. This method is blocking and thus
	 * should be the last line executed in the {@code Server}'s constructor, or placed in a
	 * separate thread.
	 */
	public void run() {
		Scanner cliIn = new Scanner(System.in);
		Log.stdout(Log.INFO, "ServerCLI", "CLI established, type \"help\"");

		// Constantly read user input and pass to the process method
		while (cliIn.hasNext())
			this.process(cliIn.nextLine());
	}


	/**
	 * Splits an input string into command line options. Splits the argument input by spaces
	 * such that the first element of the array is the command and subsequent elements are
	 * arguments to that command.
	 * <p>
	 * This method takes quoted strings into account (allowing for multi-word arguments that
	 * contain spaces). No command validation is done by this method, it is strictly a
	 * parsing routine.
	 *
	 * @param input  the input string to parse.
	 *
	 * @return a {@code List} containing the components of the command.
	 *
	 * @throws NullPointerException  if {@code input == null}
	 */
	private List<String> getopt(String input) {
		if (input == null)
			throw new NullPointerException("input was null");
		
		List<String> parsed = new ArrayList<>();

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
		
		// Since there is no space at the end of the input string, add the final argument
		// being built outside of the loop
		parsed.add(arg);

		return parsed;
	}


	/**
	 * Processes an input string as a command and calls the appropriate routine to handle the
	 * command.
	 *
	 * @param input  the input string to process.
	 */
	private void process(String input) {
		// Get the options for input formatted
		List<String> args = this.getopt(input);
		if (args.size() == 0) {
			Log.stdout(Log.ERROR, "ServerCLI", "Invalid entry");
			return;
		}

		 // Remove the first element (command name) and assign the value to "cmd"
		String cmd = args.remove(0);

		// Call routine based on the command
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


	/**
	 * Prints the help message.
	 */
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
		Log.stdout(Log.INFO, "ServerCLI",
				   "\t\truns perft test suite, optionally with a start/end test");
		Log.stdout(Log.INFO, "ServerCLI",
				   "\t\tand the ability to print node count after each move");
		Log.stdout(Log.INFO, "ServerCLI", "\taddr");
		Log.stdout(Log.INFO, "ServerCLI", "\t\tprints the server address and port");
		Log.stdout(Log.INFO, "ServerCLI", "\thelp");
		Log.stdout(Log.INFO, "ServerCLI", "\t\tprint this message");
	}


	/**
	 * Sets the chess board position from a FEN string. If the command fails for any
	 * reason, the call is terminated and ignored.
	 *
	 * @param args  command line arguments.
	 */
	private void set(List<String> args) {
		if (args.size() == 0) {
			Log.stdout(Log.ERROR, "ServerCLI", "Missing argument for set <fen>");
			return;
		}

		String arg = args.get(0);
		BoardInfo setInfo;
		try {
			setInfo = FenUtility.informationFromFen(arg);
		}
		catch (RuntimeException e) {
			Log.stdout(Log.ERROR, "ServerCLI", "Invalid argument for set: " + arg + " (" + e + ")");
			return;
		}
		Log.stdout(Log.INFO, "ServerCLI", "Updating board...");
		this.server.setBoardInfo(setInfo);
	}


	/**
	 * Get the chess board position as a FEN string. The result is printed to the standard output 
	 * using the logging utility of {@code jnet}. If the command fails for any reason, the call is 
	 * terminated and ignored.
	 *
	 * @see jnet.Log
	 */
	private void get() {
		BoardInfo boardInfo = this.server.getBoardInfo();
		if (boardInfo != null)
			Log.stdout(Log.INFO, "ServerCLI", boardInfo.toString());
		else
			Log.stdout(Log.ERROR, "ServerCLI", "Cannot get board information, null found");
	}


	/**
	 * Resets the chess board position to the starting FEN string. If the command fails for any
	 * reason, the call is terminated and ignored.
	 */
	private void reset() {
		List<String> args = new ArrayList<>();
		args.add(Board.START_FEN);
		this.set(args);
	}


	/**
	 * Runs performance tests. If the command fails for any reason, the call is terminated and 
	 * ignored.
	 *
	 * @param args  command line arguments.
	 */
	private void perft(List<String> args) {
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
			Log.stdout(Log.ERROR, "ServerCLI",
					   "Invalid argument for perft -s or -e, must be an integer");
			return;
		}
		
		Log.stdout(Log.INFO, "ServerCLI",
				   "Running perft test suite, this may take several minutes...");
		PerftTest.run(start, end, divide);
	}


	/**
	 * Prints the IP address and port the server is hosted on. If the command fails for any
	 * reason, the call is terminated and ignored.
	 *
	 * @param args  command line arguments.
	 */
	private void addr() {
		Log.stdout(Log.INFO, "ServerCLI", "Server can be reached at: " +
				   this.server.getIP() + ":" + this.server.getPort());
	}
	
}
