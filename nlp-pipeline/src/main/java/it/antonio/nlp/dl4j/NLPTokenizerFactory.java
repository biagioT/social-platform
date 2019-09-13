package it.antonio.nlp.dl4j;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import it.antonio.nlp.NLPPipeline;
import it.antonio.nlp.SentenceRewriter;
import it.antonio.nlp.commons.Token;

public class NLPTokenizerFactory implements TokenizerFactory , Serializable{

	private static final long serialVersionUID = 1L;
	private SentenceRewriter rewriter = new SentenceRewriter();
	private NLPPipeline pipeline;
	
	public NLPTokenizerFactory(NLPPipeline pipeline) {
		super();
		this.pipeline = pipeline;
	}

	@Override
	public Tokenizer create(String toTokenize) {
		List<Token> tokens = pipeline.tokens(toTokenize);
		 
		List<String> rewrited = rewriter.rewrite(tokens);
		
		return new NLPTokenizer(tokens, rewrited);
	}

	@Override
	public Tokenizer create(InputStream toTokenize) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTokenPreProcessor(TokenPreProcess preProcessor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TokenPreProcess getTokenPreProcessor() {
		return null;
	}
	
	public static class NLPTokenizer implements Tokenizer{
		
		private List<Token> tokens; 
		private List<String> rewrited;
		private Iterator<String> it;
		
		public NLPTokenizer(List<Token> tokens, List<String> rewrited) {
			super();
			this.tokens = tokens;
			this.rewrited = rewrited;
			this.it = rewrited.iterator();
			
		}

		@Override
		public void setTokenPreProcessor(TokenPreProcess tokenPreProcessor) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String nextToken() {
			String token =  it.next();
			return token;
		}
		
		@Override
		public boolean hasMoreTokens() {
			return it.hasNext();
		}
		
		@Override
		public List<String> getTokens() {
			return rewrited;
		}
		public List<Token> getObjectTokens() {
			return tokens;
		}
		
		@Override
		public int countTokens() {
			return tokens.size();
		}
		
	}

}
