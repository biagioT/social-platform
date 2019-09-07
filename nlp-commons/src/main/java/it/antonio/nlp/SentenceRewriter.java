package it.antonio.nlp;

import java.util.ArrayList;
import java.util.List;

import it.antonio.nlp.NLPPipeline.Token;

public class SentenceRewriter {


	public List<String> rewrite(List<Token> tokens) {
		
		List<String> rewerited = new ArrayList<String>();
		
		for(Token token: tokens) {
			
			if(token.lemma != null) {
				rewerited.add(token.lemma);
			} else if(token.specialTokenType != null) {
				rewerited.add(token.specialTokenType);
			} else if(token.nerTag != null) {
				rewerited.add(token.nerTag);
			} else {
				rewerited.add(token.word);
			}
			
		}
		return rewerited;
		/*
    	String[] tokens = tokenizer.tokenize(sentence);
        String[] tokensWithNer = nerFinder.convert(tokens);
    	
    	for(int i = 0; i < tokensWithNer.length; i++) {
    		if(tokensWithNer[i].startsWith("#")) {
    			tokensWithNer[i]=tokensWithNer[i].substring(1);
    		}
    		if(tokensWithNer[i].startsWith("@")) {
    			tokensWithNer[i]=tokensWithNer[i].substring(1);
    		}
    		
    		String lemma = lemmatizer.lemma(tokensWithNer[i]);
			
			if(lemma != null) {
				tokensWithNer[i] = lemma;
			}
			
			
			//bld.append(tokensWithNer[i] +" ");
				
		}
    	
    	//String res = bld.toString();
    	//bld.setLength(0);
    	
    	return tokensWithNer;*/
		
	
    }
    
}
