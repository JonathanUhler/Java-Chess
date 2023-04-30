package tests;

public class Perft {

	public String fenString;
	public int[] nodes;
	

	public Perft(String perft) {
		String[] perftSplit = perft.split(" ;");
		
		this.fenString = perftSplit[0];
		
		this.nodes = new int[6];
		for (int i = 0; i < EngineTest.MAX_DEPTH; i++) {
			String nodeInfo = perftSplit[i + 1];
			String nodeStr = nodeInfo.split(" ")[1];
			int node = Integer.parseInt(nodeStr);
			nodes[i] = node;
		}
	}

}
