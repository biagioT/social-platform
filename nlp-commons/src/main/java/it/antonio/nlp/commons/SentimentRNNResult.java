package it.antonio.nlp.commons;

import java.util.List;

public class SentimentRNNResult {
	private double unbiased; 
	private double positive; 
	private double negative; 
	private double mixedFeelings;
	
	private int notFoundInWordVec;
	
	private List<Token> tokens;
	
	public SentimentRNNResult(double unbiased, double positive, double negative, double mixedFeelings,
			int notFoundInWordVec, List<Token> tokens) {
		super();
		this.unbiased = unbiased;
		this.positive = positive;
		this.negative = negative;
		this.mixedFeelings = mixedFeelings;
		this.notFoundInWordVec = notFoundInWordVec;
		this.tokens = tokens;
	}
	
	public double getUnbiased() {
		return unbiased;
	}
	public double getPositive() {
		return positive;
	}
	public double getNegative() {
		return negative;
	}
	public double getMixedFeelings() {
		return mixedFeelings;
	}

	public int getNotFoundInWordVec() {
		return notFoundInWordVec;
	}

	public List<Token> getTokens() {
		return tokens;
	} 
	
	
}
