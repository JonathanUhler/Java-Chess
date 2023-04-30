import engine.move.*;
import engine.fen.*;
import util.*;
import engine.board.*;
import engine.piece.*;
import server.*;
import java.util.*;

public class Test {
	public static void main(String[] args) {
		//Server server = new Server(Server.IP_ADDR, Server.PORT);


		//System.out.println(Integer.toHexString(NetUtility.crc32("This is a test of the crc32 routine!!! Yay!!")));
		String s = "This is a test of crc";
		String scrc = CRCUtility.crcAdd(s);
		System.out.println(scrc);
		System.out.println(CRCUtility.crcCheck(scrc));
		
		
		// GENERATE AND PRINT POSITION
		/*
		BoardInfo bi = FenUtility.informationFromFen("8/8/8/8/8/8/4n3/R3K2R w KQkq - 0 1");
		Board b = new Board(bi);
		System.out.println(b.getInfo());

		Move m1 = new Move(new Coordinate(4, 0), new Coordinate(4, 1));
		b.makeMove(m1);
		System.out.println(b.getInfo());

		Move m2 = new Move(new Coordinate(4, 1), new Coordinate(4, 2));
		b.makeMove(m2);
		System.out.println(b.getInfo());
		*/


		/*
		// GENERATE POSSIBLE MOVES
		ArrayList<Move> marr = MoveGenerator.generateLegalMoves(b);

		
		// PRINT OUT BOARD WITH MOVES HIGHLIGHTED AS "X"
		System.out.println("\n----------------------------------------\nTest.java\n----------------------------------------");
		ArrayList<Coordinate> ends = new ArrayList<>();
		for (Move m : marr) {
			ends.add(m.getEndTile());
		}
		for (int y = 7; y >= 0; y--) {
			for (int x = 0; x < 8; x++) {
				if (ends.contains(new Coordinate(x, y))) {
					System.out.print("X ");
				}
				else {
					Piece piece = b.getPiece(new Coordinate(x, y));
					if (piece == null)
						System.out.print(". ");
					else
						System.out.print(piece + " ");
				}
			}
			System.out.println();
		}

		
		// PRINT OUT ALL MOVES
		for (Move m : marr)
			System.out.println(m);
		System.out.println();
		System.out.println("Generated " + marr.size() + " moves.");
		*/

		
	}
}
