package it.antonio.sentiment;

public interface Constants {
	
	// word2vec
	public static final int WORD_SIZE = 100;
	public static final String FILE_WORD2VEC = "../nlp-trained-models/word2vec.dat";
	
	// rnn network
	public static final String FILE_NETWORK = "../nlp-trained-models/network.dat";
	public static final String FILE_NETWORK_STATS = "../nlp-trained-models/network_stats.dat";
	
	public static final int HIDDEN_LAYER_SIZE = 32;
	public static final int NUM_LABEL = 4;

}
