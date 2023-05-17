package tests;


import jnet.Log;
import engine.move.Move;
import engine.move.MoveGenerator;
import engine.board.Board;
import engine.board.BoardInfo;
import engine.fen.FenUtility;
import java.util.List;
import java.util.ArrayList;


/**
 * PERF(ormance) T(esting) framework for this chess engine.
 *
 * @author Jonathan Uhler
 */
public class PerftTest {

	/** 
	 * The maximum depth positions should be tested with. MAX_DEPTH should normally be set to 3, 
	 * which runs in ~1.5 minutes. 4 takes >10 minutes to run, although is good to do after major 
	 * changes to check accuracy. The goal of having so many tests is that any strange situations 
	 * will arise at least once with MAX_DEPTH = 3, so having a high depth is not really as 
	 * important if there are enough tests to cover a wide range of positions.
	 */
	private static final int MAX_DEPTH = 3;
	
	/** Local list of every test, easier than reading a file. */
	public static final List<String> TESTS = new ArrayList<>() {{
			add("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1;" +
				"20;400;8902;197281;4865609;119060324");
			add("4k3/8/8/8/8/8/8/4K2R w K - 0 1;15;66;1197;7059;133987;764643");
			add("4k3/8/8/8/8/8/8/R3K3 w Q - 0 1;16;71;1287;7626;145232;846648");
			add("4k2r/8/8/8/8/8/8/4K3 w k - 0 1;5;75;459;8290;47635;899442");
			add("r3k3/8/8/8/8/8/8/4K3 w q - 0 1;5;80;493;8897;52710;1001523");
			add("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1;26;112;3189;17945;532933;2788982");
			add("r3k2r/8/8/8/8/8/8/4K3 w kq - 0 1;5;130;782;22180;118882;3517770");
			add("8/8/8/8/8/8/6k1/4K2R w K - 0 1;12;38;564;2219;37735;185867");
			add("8/8/8/8/8/8/1k6/R3K3 w Q - 0 1;15;65;1018;4573;80619;413018");
			add("4k2r/6K1/8/8/8/8/8/8 w k - 0 1;3;32;134;2073;10485;179869");
			add("r3k3/1K6/8/8/8/8/8/8 w q - 0 1;4;49;243;3991;20780;367724");
			add("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1;26;568;13744;314346;7594526;179862938");
			add("r3k2r/8/8/8/8/8/8/1R2K2R w Kkq - 0 1;25;567;14095;328965;8153719;195629489");
			add("r3k2r/8/8/8/8/8/8/2R1K2R w Kkq - 0 1;25;548;13502;312835;7736373;184411439");
			add("r3k2r/8/8/8/8/8/8/R3K1R1 w Qkq - 0 1;25;547;13579;316214;7878456;189224276");
			add("1r2k2r/8/8/8/8/8/8/R3K2R w KQk - 0 1;26;583;14252;334705;8198901;198328929");
			add("2r1k2r/8/8/8/8/8/8/R3K2R w KQk - 0 1;25;560;13592;317324;7710115;185959088");
			add("r3k1r1/8/8/8/8/8/8/R3K2R w KQq - 0 1;25;560;13607;320792;7848606;190755813");
			add("4k3/8/8/8/8/8/8/4K2R b K - 0 1;5;75;459;8290;47635;899442");
			add("4k3/8/8/8/8/8/8/R3K3 b Q - 0 1;5;80;493;8897;52710;1001523");
			add("4k2r/8/8/8/8/8/8/4K3 b k - 0 1;15;66;1197;7059;133987;764643");
			add("r3k3/8/8/8/8/8/8/4K3 b q - 0 1;16;71;1287;7626;145232;846648");
			add("4k3/8/8/8/8/8/8/R3K2R b KQ - 0 1;5;130;782;22180;118882;3517770");
			add("r3k2r/8/8/8/8/8/8/4K3 b kq - 0 1;26;112;3189;17945;532933;2788982");
			add("8/8/8/8/8/8/6k1/4K2R b K - 0 1;3;32;134;2073;10485;179869");
			add("8/8/8/8/8/8/1k6/R3K3 b Q - 0 1;4;49;243;3991;20780;367724");
			add("4k2r/6K1/8/8/8/8/8/8 b k - 0 1;12;38;564;2219;37735;185867");
			add("r3k3/1K6/8/8/8/8/8/8 b q - 0 1;15;65;1018;4573;80619;413018");
			add("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1;26;568;13744;314346;7594526;179862938");
			add("r3k2r/8/8/8/8/8/8/1R2K2R b Kkq - 0 1;26;583;14252;334705;8198901;198328929");
			add("r3k2r/8/8/8/8/8/8/2R1K2R b Kkq - 0 1;25;560;13592;317324;7710115;185959088");
			add("r3k2r/8/8/8/8/8/8/R3K1R1 b Qkq - 0 1;25;560;13607;320792;7848606;190755813");
			add("1r2k2r/8/8/8/8/8/8/R3K2R b KQk - 0 1;25;567;14095;328965;8153719;195629489");
			add("2r1k2r/8/8/8/8/8/8/R3K2R b KQk - 0 1;25;548;13502;312835;7736373;184411439");
			add("r3k1r1/8/8/8/8/8/8/R3K2R b KQq - 0 1;25;547;13579;316214;7878456;189224276");
			add("8/1n4N1/2k5/8/8/5K2/1N4n1/8 w - - 0 1;14;195;2760;38675;570726;8107539");
			add("8/1k6/8/5N2/8/4n3/8/2K5 w - - 0 1;11;156;1636;20534;223507;2594412");
			add("8/8/4k3/3Nn3/3nN3/4K3/8/8 w - - 0 1;19;289;4442;73584;1198299;19870403");
			add("K7/8/2n5/1n6/8/8/8/k6N w - - 0 1;3;51;345;5301;38348;588695");
			add("k7/8/2N5/1N6/8/8/8/K6n w - - 0 1;17;54;835;5910;92250;688780");
			add("8/1n4N1/2k5/8/8/5K2/1N4n1/8 b - - 0 1;15;193;2816;40039;582642;8503277");
			add("8/1k6/8/5N2/8/4n3/8/2K5 b - - 0 1;16;180;2290;24640;288141;3147566");
			add("8/8/3K4/3Nn3/3nN3/4k3/8/8 b - - 0 1;4;68;1118;16199;281190;4405103");
			add("K7/8/2n5/1n6/8/8/8/k6N b - - 0 1;17;54;835;5910;92250;688780");
			add("k7/8/2N5/1N6/8/8/8/K6n b - - 0 1;3;51;345;5301;38348;588695");
			add("B6b/8/8/8/2K5/4k3/8/b6B w - - 0 1;17;278;4607;76778;1320507;22823890");
			add("8/8/1B6/7b/7k/8/2B1b3/7K w - - 0 1;21;316;5744;93338;1713368;28861171");
			add("k7/B7/1B6/1B6/8/8/8/K6b w - - 0 1;21;144;3242;32955;787524;7881673");
			add("K7/b7/1b6/1b6/8/8/8/k6B w - - 0 1;7;143;1416;31787;310862;7382896");
			add("B6b/8/8/8/2K5/5k2/8/b6B b - - 0 1;6;106;1829;31151;530585;9250746");
			add("8/8/1B6/7b/7k/8/2B1b3/7K b - - 0 1;17;309;5133;93603;1591064;29027891");
			add("k7/B7/1B6/1B6/8/8/8/K6b b - - 0 1;7;143;1416;31787;310862;7382896");
			add("K7/b7/1b6/1b6/8/8/8/k6B b - - 0 1;21;144;3242;32955;787524;7881673");
			add("7k/RR6/8/8/8/8/rr6/7K w - - 0 1;19;275;5300;104342;2161211;44956585");
			add("R6r/8/8/2K5/5k2/8/8/r6R w - - 0 1;36;1027;29215;771461;20506480;525169084");
			add("7k/RR6/8/8/8/8/rr6/7K b - - 0 1;19;275;5300;104342;2161211;44956585");
			add("R6r/8/8/2K5/5k2/8/8/r6R b - - 0 1;36;1027;29227;771368;20521342;524966748");
			add("6kq/8/8/8/8/8/8/7K w - - 0 1;2;36;143;3637;14893;391507");
			add("6KQ/8/8/8/8/8/8/7k b - - 0 1;2;36;143;3637;14893;391507");
			add("K7/8/8/3Q4/4q3/8/8/7k w - - 0 1;6;35;495;8349;166741;3370175");
			add("6qk/8/8/8/8/8/8/7K b - - 0 1;22;43;1015;4167;105749;419369");
			add("6KQ/8/8/8/8/8/8/7k b - - 0 1;2;36;143;3637;14893;391507");
			add("K7/8/8/3Q4/4q3/8/8/7k b - - 0 1;6;35;495;8349;166741;3370175");
			add("8/8/8/8/8/K7/P7/k7 w - - 0 1;3;7;43;199;1347;6249");
			add("8/8/8/8/8/7K/7P/7k w - - 0 1;3;7;43;199;1347;6249");
			add("K7/p7/k7/8/8/8/8/8 w - - 0 1;1;3;12;80;342;2343");
			add("7K/7p/7k/8/8/8/8/8 w - - 0 1;1;3;12;80;342;2343");
			add("8/2k1p3/3pP3/3P2K1/8/8/8/8 w - - 0 1;7;35;210;1091;7028;34834");
			add("8/8/8/8/8/K7/P7/k7 b - - 0 1;1;3;12;80;342;2343");
			add("8/8/8/8/8/7K/7P/7k b - - 0 1;1;3;12;80;342;2343");
			add("K7/p7/k7/8/8/8/8/8 b - - 0 1;3;7;43;199;1347;6249");
			add("7K/7p/7k/8/8/8/8/8 b - - 0 1;3;7;43;199;1347;6249");
			add("8/2k1p3/3pP3/3P2K1/8/8/8/8 b - - 0 1;5;35;182;1091;5408;34822");
			add("8/8/8/8/8/4k3/4P3/4K3 w - - 0 1;2;8;44;282;1814;11848");
			add("4k3/4p3/4K3/8/8/8/8/8 b - - 0 1;2;8;44;282;1814;11848");
			add("8/8/7k/7p/7P/7K/8/8 w - - 0 1;3;9;57;360;1969;10724");
			add("8/8/k7/p7/P7/K7/8/8 w - - 0 1;3;9;57;360;1969;10724");
			add("8/8/3k4/3p4/3P4/3K4/8/8 w - - 0 1;5;25;180;1294;8296;53138");
			add("8/3k4/3p4/8/3P4/3K4/8/8 w - - 0 1;8;61;483;3213;23599;157093");
			add("8/8/3k4/3p4/8/3P4/3K4/8 w - - 0 1;8;61;411;3213;21637;158065");
			add("k7/8/3p4/8/3P4/8/8/7K w - - 0 1;4;15;90;534;3450;20960");
			add("8/8/7k/7p/7P/7K/8/8 b - - 0 1;3;9;57;360;1969;10724");
			add("8/8/k7/p7/P7/K7/8/8 b - - 0 1;3;9;57;360;1969;10724");
			add("8/8/3k4/3p4/3P4/3K4/8/8 b - - 0 1;5;25;180;1294;8296;53138");
			add("8/3k4/3p4/8/3P4/3K4/8/8 b - - 0 1;8;61;411;3213;21637;158065");
			add("8/8/3k4/3p4/8/3P4/3K4/8 b - - 0 1;8;61;483;3213;23599;157093");
			add("k7/8/3p4/8/3P4/8/8/7K b - - 0 1;4;15;89;537;3309;21104");
			add("7k/3p4/8/8/3P4/8/8/K7 w - - 0 1;4;19;117;720;4661;32191");
			add("7k/8/8/3p4/8/8/3P4/K7 w - - 0 1;5;19;116;716;4786;30980");
			add("k7/8/8/7p/6P1/8/8/K7 w - - 0 1;5;22;139;877;6112;41874");
			add("k7/8/7p/8/8/6P1/8/K7 w - - 0 1;4;16;101;637;4354;29679");
			add("k7/8/8/6p1/7P/8/8/K7 w - - 0 1;5;22;139;877;6112;41874");
			add("k7/8/6p1/8/8/7P/8/K7 w - - 0 1;4;16;101;637;4354;29679");
			add("k7/8/8/3p4/4p3/8/8/7K w - - 0 1;3;15;84;573;3013;22886");
			add("k7/8/3p4/8/8/4P3/8/7K w - - 0 1;4;16;101;637;4271;28662");
			add("7k/3p4/8/8/3P4/8/8/K7 b - - 0 1;5;19;117;720;5014;32167");
			add("7k/8/8/3p4/8/8/3P4/K7 b - - 0 1;4;19;117;712;4658;30749");
			add("k7/8/8/7p/6P1/8/8/K7 b - - 0 1;5;22;139;877;6112;41874");
			add("k7/8/7p/8/8/6P1/8/K7 b - - 0 1;4;16;101;637;4354;29679");
			add("k7/8/8/6p1/7P/8/8/K7 b - - 0 1;5;22;139;877;6112;41874");
			add("k7/8/6p1/8/8/7P/8/K7 b - - 0 1;4;16;101;637;4354;29679");
			add("k7/8/8/3p4/4p3/8/8/7K b - - 0 1;5;15;102;569;4337;22579");
			add("k7/8/3p4/8/8/4P3/8/7K b - - 0 1;4;16;101;637;4271;28662");
			add("7k/8/8/p7/1P6/8/8/7K w - - 0 1;5;22;139;877;6112;41874");
			add("7k/8/p7/8/8/1P6/8/7K w - - 0 1;4;16;101;637;4354;29679");
			add("7k/8/8/1p6/P7/8/8/7K w - - 0 1;5;22;139;877;6112;41874");
			add("7k/8/1p6/8/8/P7/8/7K w - - 0 1;4;16;101;637;4354;29679");
			add("k7/7p/8/8/8/8/6P1/K7 w - - 0 1;5;25;161;1035;7574;55338");
			add("k7/6p1/8/8/8/8/7P/K7 w - - 0 1;5;25;161;1035;7574;55338");
			add("3k4/3pp3/8/8/8/8/3PP3/3K4 w - - 0 1;7;49;378;2902;24122;199002");
			add("7k/8/8/p7/1P6/8/8/7K b - - 0 1;5;22;139;877;6112;41874");
			add("7k/8/p7/8/8/1P6/8/7K b - - 0 1;4;16;101;637;4354;29679");
			add("7k/8/8/1p6/P7/8/8/7K b - - 0 1;5;22;139;877;6112;41874");
			add("7k/8/1p6/8/8/P7/8/7K b - - 0 1;4;16;101;637;4354;29679");
			add("k7/7p/8/8/8/8/6P1/K7 b - - 0 1;5;25;161;1035;7574;55338");
			add("k7/6p1/8/8/8/8/7P/K7 b - - 0 1;5;25;161;1035;7574;55338");
			add("3k4/3pp3/8/8/8/8/3PP3/3K4 b - - 0 1;7;49;378;2902;24122;199002");
			add("8/Pk6/8/8/8/8/6Kp/8 w - - 0 1;11;97;887;8048;90606;1030499");
			add("n1n5/1Pk5/8/8/8/8/5Kp1/5N1N w - - 0 1;24;421;7421;124608;2193768;37665329");
			add("8/PPPk4/8/8/8/8/4Kppp/8 w - - 0 1;18;270;4699;79355;1533145;28859283");
			add("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N w - - 0 1;24;496;9483;182838;3605103;71179139");
			add("8/Pk6/8/8/8/8/6Kp/8 b - - 0 1;11;97;887;8048;90606;1030499");
			add("n1n5/1Pk5/8/8/8/8/5Kp1/5N1N b - - 0 1;24;421;7421;124608;2193768;37665329");
			add("8/PPPk4/8/8/8/8/4Kppp/8 b - - 0 1;18;270;4699;79355;1533145;28859283");
			add("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1;24;496;9483;182838;3605103;71179139");
	}};


	/**
	 * Runs a single test from a starting position.
	 * <p>
	 * General algorithm based on C code: https://www.chessprogramming.org/Perft#Perft_function.
	 *
	 * @param board   the {@code Board} object to play moves on.
	 * @param depth   the current depth of the test (this method is recursive).
	 * @param divide  an option that prints the found nodes after each move made (as opposed to
	 *                only printing after all moves have been made for the entire test). Very 
	 *                verbose, useful for debug.
	 *
	 * @return the number of nodes found after making all the available moves through 
	 *         {@code PerftTest.MAX_DEPTH}.
	 */
	private static int perft(Board board, int depth, boolean divide) {
		// Break case
		if (depth == 0)
			return 1;

		int nodes = 0;
		List<Move> moves = MoveGenerator.generateLegalMoves(board.getInfo());
		// Loop through every possible move at this depth, make the move, then recurse, then
		// unmake the move to preserve the board structure
		for (Move move : moves) {
			board.makeMove(move);
			int prevNodes = nodes;
			nodes += PerftTest.perft(board, depth - 1, divide);
			if (divide && depth == PerftTest.MAX_DEPTH)
				Log.stdout(Log.DEBUG, "PerftTest", "\tMove: " + move +
						   "\tNodes: " + (nodes - prevNodes));
			board.unmakeMove();
		}

		return nodes;
	}


	/**
	 * Runs performance tests.
	 *
	 * @param start   the first test number to run, inclusive.
	 * @param end     the last test number to run, exclusive.
	 * @param divide  an option that prints the found nodes after each move made (as opposed to
	 *                only printing after all moves have been made for the entire test). Very 
	 *                verbose, useful for debug.
	 */
	public static void run(int start, int end, boolean divide) {
		// Validate min and max test number
		if (start < 0) {
			Log.stdlog(Log.WARN, "PerftTest",
					   "Specified start test # is too small, defaulting to first test");
			start = 0;
		}
		if (end >= PerftTest.TESTS.size()) {
			Log.stdlog(Log.WARN, "PerftTest",
					   "Specified end test # is too large, defaulting to last test");
			end = PerftTest.TESTS.size() - 1;
		}

		// Track number of tests passed and failed for total percentage passed
		int numPassed = 0;
		int numFailed = 0;
		long testingTime = 0;

		// Loop through the specified test range
		for (int i = start; i <= end; i++) {
			String test = PerftTest.TESTS.get(i);

			// Split the test string by the semicolon delimiter. Check to see if the test can be
			// graded. If the maximum depth is not within the # nodes/depth listed in the string,
			// then that test cannot be graded since the expected value is not known.
			String[] testSplit = test.split(";");
			if (testSplit.length != 7 ||
				PerftTest.MAX_DEPTH < 1 ||
				PerftTest.MAX_DEPTH > testSplit.length - 1)
			{
				Log.stdlog(Log.WARN, "PerftTest",
						   "Test skipped, improper length or MAX_DEPTH is out of range");
				continue;
			}

			// Board setup from the fen string
			String fenString = testSplit[0];
			BoardInfo boardInfo = FenUtility.informationFromFen(fenString);
			Board board = new Board(boardInfo);

			// Running the test and computing total time used
			long startTime = System.currentTimeMillis();
			int numTotalNodes = PerftTest.perft(board, PerftTest.MAX_DEPTH, divide);
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;

			// Get the expected result
			int numExpectedNodes;
			String numExpectedNodesString = testSplit[PerftTest.MAX_DEPTH];
			try {
				numExpectedNodes = Integer.parseInt(numExpectedNodesString);
			}
			catch (NumberFormatException e) {
				Log.stdlog(Log.WARN, "PerftTest",
						   "test eval skipped, could not parse expected nodes as int");
				continue;
			}

			// Determine if the test was passed and print messages/update values
			boolean passed = (numTotalNodes == numExpectedNodes);
			Log.stdout(Log.DEBUG, "PerftTest",
					   "Test: " + i +
					   "\tDepth: " + PerftTest.MAX_DEPTH +
					   "\tResult: " + numTotalNodes +
					   "\tTime: " + totalTime + "ms" +
					   "\t--  " +
					   ((passed) ? "Passed" : "FAILED (expected " + numExpectedNodes + ")"));
			if (passed)
				numPassed++;
			else
				numFailed++;
			testingTime += totalTime;
		}

		// Print total percentage passed/failed after all requested tests have been completed
		Log.stdout(Log.DEBUG, "PerftTest", "Passed: " + numPassed +
				   "\tFailed: " + numFailed +
				   "\tTotal Time: " + testingTime + "ms" +
				   "\t--  " + (numPassed * 1.0 / (numPassed + numFailed) * 1.0) * 100.0 + "%");
	}

}
