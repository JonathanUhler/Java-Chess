package server;


import jnet.JServer;
import jnet.JClientSocket;
import jnet.Log;
import engine.board.Board;
import engine.board.BoardInfo;
import engine.fen.FenUtility;
import java.io.IOException;
import javax.swing.JOptionPane;
import java.util.Map;


/**
 * Extendable server for a chess variant. This abstract class provides the framework for
 * managing a chess game through a {@code Board} object. It also provides a command line
 * interface for manipulating the board with the shell.
 * <p>
 * This class extends {@code jnet.JServer}. Children are expected to provide implementations
 * for the {@code client(Connected|Communicated|Disconnected)} methods. Any child of this
 * class created as a modification to this chess game should have the {@code package server}
 * statement, allowing access to the protected {@code getBoardInfo} and 
 * {@code setBoardInfo(engine.board.BoardInfo)} methods of this class. These can be used
 * for server-client communication.
 * <p>
 * The standard network protocol supported by this chess game is defined in the
 * {@code server.Communication} class. (Note that this class assits with formatting and parsing
 * commands, but does not guarantee data integrity; this is the responsibility of any
 * child of this server class or the graphical client view classes).
 * <p>
 * <b>IMPORTANT NOTE:</b> as defined in the documentation for {@code Board.makeMove(Move)}, 
 * it is the responsibility of the server to confirm the legality of moves before making them
 * on the board. Nothing can be guaranteed if a valid, but illegal, move is played on the board.
 * Implementations of the {@code Server} class are responsible for this because of the
 * possibility for modified move generators (in other words, the {@code MoveGenerator} class is
 * not always used for custom variants).
 * <p>
 * A basic implementation of checking for legal moves is as follows:
 * <pre>
 * {@code
 * List<Move> legalMoves = MyMoveGenerator.generateLegalMoves(super.getBoardInfo());
 * if (!legalMoves.contains(move))
 *     return;
 * }
 * </pre>
 *
 * @see server.ServerCLI
 * @see engine.board.Board
 *
 * @author Jonathan Uhler
 */
public abstract class Server extends JServer {

	/** The board state for the chess game managed by this server. */
	private Board board;
	/** The command line interface for this server. */
	private ServerCLI cli;
	

	/**
	 * Constructs a new {@code Server} object. This constructor initializes the 
	 * {@code jnet.JServer} super-class, the {@code Board} object managed by this class (which
	 * can be accessed through the board info getter and setter), and starts the cli.
	 *
	 * @param ip    the IP address to bind the server to.
	 * @param port  the port to bind the server to.
	 *
	 * @throws IOException  if a network error occurs during server startup.
	 */
	public Server(String ip, int port) throws IOException {
		super(ip, port);

		BoardInfo boardInfo = FenUtility.informationFromFen(Board.START_FEN);
		this.board = new Board(boardInfo);

		this.cli = new ServerCLI(this);
		this.cli.run();
	}


	/**
	 * Returns the board managed by this server. This method is intended to be used in order
	 * to make moves on the board from a child of this server class.
	 *
	 * @return the board managed by this server.
	 */
	protected Board getBoard() {
		return this.board;
	}


	/**
	 * Returns the <b>memory pointer</b> to the board information managed by this server.
	 *
	 * @return the <b>memory pointer</b> to the board information managed by this server.
	 *
	 * @see engine.board.Board
	 */
	protected BoardInfo getBoardInfoPointer() {
		return this.board.getInfoPointer();
	}


	/**
	 * Returns the board information managed by this server. This information may only be
	 * accessed by the {@code ServerCLI} or child classes of this server.
	 *
	 * @return the board information managed by this server.
	 */
	protected BoardInfo getBoardInfo() {
		return this.board.getInfo();
	}


	/**
	 * Sets the board information managed by this server. The board can only be modified by
	 * the {@code ServerCLI} or children of this server.
	 *
	 * @param boardInfo  the board information to set.
	 */
	protected void setBoardInfo(BoardInfo boardInfo) {
		this.board = new Board(boardInfo);
		Log.stdout(Log.INFO, "Server", "Board updated");
		this.sendBoard();
	}


	/**
	 * Sends a board state command to all connected clients. This operation is ignored
	 * if this server's board information is {@code null}.
	 */
	public void sendBoard() {
		if (this.board == null || this.board.getInfo() == null)
			return;
		
		Map<String, String> stateCmd = Communication.cmdState(this.board.getInfo());
		super.sendAll(Communication.serialize(stateCmd));
	}


	/**
	 * Sends a board state command to the specified client. This operation is ignored
	 * if the argument client socket is {@code null} or this server's board information
	 * is {@code null}.
	 *
	 * @param clientSocket  the client to send the state to.
	 */
	public void sendBoard(JClientSocket clientSocket) {
		if (clientSocket == null)
			return;
		if (this.board == null || this.board.getInfo() == null)
			return;
		
		Map<String, String> stateCmd = Communication.cmdState(this.board.getInfo());
		super.send(Communication.serialize(stateCmd), clientSocket);
	}


	/**
	 * Sends a board state command to all connected clients with the specified board.
	 * This operation is ignored if the argument {@code board} is null.
	 *
	 * @param board  the board to send.
	 */
	public void sendBoard(Board board) {
		if (board == null || board.getInfo() == null)
			return;
		
		Map<String, String> stateCmd = Communication.cmdState(board.getInfo());
		super.sendAll(Communication.serialize(stateCmd));
	}


	/**
	 * Sends a board state command to the specified client with the specified board. This
	 * operation is ignored if the argument {@code board} is null.
	 *
	 * @param board         the board to send.
	 * @param clientSocket  the client to send the state to.
	 */
	public void sendBoard(Board board, JClientSocket clientSocket) {
		if (clientSocket == null)
			return;
		if (board == null || board.getInfo() == null)
			return;
		
		Map<String, String> stateCmd = Communication.cmdState(board.getInfo());
		super.send(Communication.serialize(stateCmd), clientSocket);
	}
	

	/**
	 * Advises the user to host a server through the GUI client.
	 *
	 * @param args  command line arguments.
	 */
	public static void main(String[] args) {
		JOptionPane.showMessageDialog(null,
									  "The chess server is intended to be hosted\n" +
									  "directly from the client GUI using the \"Host Game\"\n" +
									  "button. Please start the server through that method.\n" +
									  "Join an existing game with the \"Join Game\" button.",
									  "Server Hosting", JOptionPane.PLAIN_MESSAGE);
		System.exit(0);
	}

}
