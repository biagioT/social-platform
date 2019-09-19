package it.antonio.tweet;

import java.io.BufferedReader;
import java.io.IOException;

import it.antonio.nlp.LazyIterator;

public class SentipolcSentenceIterator extends LazyIterator<TweetData>{

	
	BufferedReader reader;
	StringBuilder bldTweet= new StringBuilder();
	String line;
	int num = 0;
	

	public SentipolcSentenceIterator(BufferedReader reader) throws IOException {
		super();
		this.reader = reader;
		line = reader.readLine(); // skip first line
	}
	

	@Override
	protected TweetData computeNext() {
		try {
			line = reader.readLine();
			if(line != null) {
				String[] data = line.split(",");
				bldTweet.setLength(0);
				for(int i=8; i < data.length; i++) {
					bldTweet.append(data[i] + ",");
				}
				
		    	String tweet = bldTweet.toString();
		    	tweet = tweet.substring(1, tweet.length() - 2);
		    	
		    	data[5] = data[5].replaceAll("\"", "");
		    	data[6] = data[6].replaceAll("\"", "");
		    	
		    	boolean positive = data[5].equals("1");
		    	boolean negative = data[6].equals("1");
		    	
		    	return new TweetData(tweet, positive, negative);
		    	
			}
			return endOfData();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		
		
	}
	

	
	

}