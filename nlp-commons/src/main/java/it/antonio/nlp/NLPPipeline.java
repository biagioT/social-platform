package it.antonio.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import it.antonio.nlp.SentimentLexicon.Sentiment;

public class NLPPipeline {
	
	public static NLPPipeline create() {
		MorphitLemmatizer lemmatizer = MorphitLemmatizer.create();
		NerFinder nerFinder = NerFinder.create();
		PosTagger posTagger = PosTagger.create();
		SimpleTokenizer tokenizer = SimpleTokenizer.create();
		SentimentLexicon sentimentLexicon = SentimentLexicon.create();
		
		return new NLPPipeline(lemmatizer, nerFinder, posTagger, tokenizer, sentimentLexicon);
		
	}
	
	
	
	//private StringBuilder bld = new StringBuilder();
	
	private MorphitLemmatizer lemmatizer; 
	private NerFinder nerFinder;
	private PosTagger posTagger;
	private SimpleTokenizer tokenizer;
	private SentimentLexicon sentimentLexicon;
	
    
    public NLPPipeline(MorphitLemmatizer lemmatizer, NerFinder nerFinder, PosTagger posTagger, SimpleTokenizer tokenizer,
			SentimentLexicon sentimentLexicon) {
		super();
		this.lemmatizer = lemmatizer;
		this.nerFinder = nerFinder;
		this.posTagger = posTagger;
		this.tokenizer = tokenizer;
		this.sentimentLexicon = sentimentLexicon;
	}

	public List<Token> tokens(String sentence){
    	List<Token> ret = new ArrayList<>();
    	
    	String[] tokens = tokenizer.tokenize(sentence);
    	String[] specials = tokenizer.computeSpecials(tokens); 
    	
    	String[] posTags = posTagger.tagSimple(tokens);
    	
        String[] ners = nerFinder.convert(tokens);
        
    	for(int i = 0; i < tokens.length; i++) {
    		String simpleToken = tokens[i];
    		
    		// normalization
    		if(simpleToken.startsWith("#")) {
    			simpleToken=simpleToken.substring(1);
    		}
    		if(simpleToken.startsWith("@")) {
    			simpleToken=simpleToken.substring(1);
    		}
    		
    		String lemma = lemmatizer.lemma(simpleToken);
			
			Sentiment sentiment = sentimentLexicon.sentiment(lemma);
			
			ret.add(new Token(tokens[i], lemma, ners[i], posTags[i], specials[i], sentiment));
				
		}
    	
    	return ret;
    	
    	
    }
    
    public static String print(List<Token> tokens) {
    	StringBuilder line1 = new StringBuilder();
    	StringBuilder line2 = new StringBuilder();
    	StringBuilder line3 = new StringBuilder();
    	StringBuilder line4 = new StringBuilder();
        StringBuilder line5 = new StringBuilder();
    	StringBuilder line6 = new StringBuilder();
    	
    	tokens.forEach(token -> {
    		String sentiment = token.sentiment != null ? token.sentiment.toString() : null;
    		long max = max(token.word, token.lemma, token.nerTag, token.specialTokenType, sentiment);
    		
    		line1.append(fill(token.word, max) + " ");
    		line2.append(fill(token.lemma, max) + " ");
    		line3.append(fill(token.posTag, max) + " ");
    		line4.append(fill(token.nerTag, max) + " ");
    		line5.append(fill(token.specialTokenType, max) + " ");
    		line6.append(fill(sentiment, max) + " ");
    		
    	});
    	
    	
		return line1 + "\n" + line2 + "\n"+ line3 + "\n"+ line4 + "\n"+ line5 + "\n"+ line6 + "\n";
    	
    }
    
    private static String fill(String word, long max) {
    	word = word != null ? word : "";
    	return word + IntStream.range(0, (int) (max - word.length())).mapToObj(i-> " ").collect(Collectors.joining(""));
    }
    
    private static int max(String...obj ) {
    	return Collections.max(Arrays.stream(obj).map(w-> w != null ? w.length() : 0).collect(Collectors.toList()));
    }
    
    
    
    
    
    
    
    public static class Token {
    	public final String word;
    	public final String lemma;
    	public final String posTag;
    	public final String nerTag;
    	public final String specialTokenType;
    	public final Sentiment sentiment;
    	
		public Token(String word, String lemma, String nerTag, String posTag, String specialTokenType, Sentiment sentiment) {
			super();
			this.word = word;
			this.lemma = lemma;
			this.nerTag = nerTag;
			this.posTag = posTag;
			this.specialTokenType = specialTokenType;
			this.sentiment = sentiment;
		}
		
		
    	
    }
}
