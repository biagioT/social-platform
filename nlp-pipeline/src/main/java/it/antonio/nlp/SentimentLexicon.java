package it.antonio.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import it.antonio.nlp.commons.Token.Sentiment;

public class SentimentLexicon {
	
	private Map<String, double[]> data = new HashMap<>();
	
	
	
	public static SentimentLexicon create() {

		try {
			
			InputStream streamNegative = SentimentLexicon.class.getClassLoader().getResourceAsStream("sentix");
			
			BufferedReader reader =   new BufferedReader(new InputStreamReader(streamNegative));
			
			Map<String, double[]> data = new HashMap<>();
			
			String line;
			while ((line = reader.readLine()) != null) {
				String[] strings = line.split("\\t");
				
				String word = strings[0];
				
				double positive =  Double.valueOf(strings[3]);
				double negative =  Double.valueOf(strings[4]);
				double polarity =  Double.valueOf(strings[5]);
				double intensity =  Double.valueOf(strings[6]);
				
				data.put(word, new double[] {positive, negative, polarity, intensity});
				
			
			}
			
			return new SentimentLexicon(data);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading semantic files", e);
		}
	}
	
	public SentimentLexicon(Map<String, double[]> data) {
		super();
		this.data = data;
	}


	public Sentiment sentiment(String word) {
		double[] res = data.get(word);
		if(res != null) {
			return new Sentiment(res[0], res[1],res[2], res[3]); 
		} else {
			return null;
		} 
	}


}
