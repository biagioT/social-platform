package it.antonio.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class StopWords implements Serializable{
	
	private static final long serialVersionUID = 5036470238101937474L;
	Set<String> stopwords; 
	
	public StopWords(Set<String> stopwords) {
		super();
		this.stopwords = stopwords;
	}

	public static StopWords create() {

		try {
			
			InputStream stream = MorphitLemmatizer.class.getClassLoader().getResourceAsStream("stopwords.txt");
			BufferedReader wordFile = new BufferedReader(new InputStreamReader(stream));

			String line;
			
			Set<String> words = new HashSet<>();
			
			while ((line = wordFile.readLine())!= null) {
				
				if(!line.trim().isEmpty()) {
					words.add(line);
				}
			}
			
			return new StopWords(words);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading wiki_ann_ner_training_model file", e);
		}
	}

	public boolean isStopWord(String word) {
		return stopwords.contains(word);
	}
}
