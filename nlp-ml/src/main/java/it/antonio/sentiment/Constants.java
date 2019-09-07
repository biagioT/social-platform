package it.antonio.sentiment;

public interface Constants {
	
	// word2vec
	public static final int WORD_SIZE = 100;
	public static final String FILE_WORD2VEC = "src/main/resources/word2vec.dat";
	
	// rnn network
	public static final String FILE_NETWORK = "src/main/resources/network.dat";
	public static final String FILE_NETWORK_STATS = "src/main/resources/network_stats.dat";
	
	public static final int HIDDEN_LAYER_SIZE = 32;
	public static final int NUM_LABEL = 4;

}
