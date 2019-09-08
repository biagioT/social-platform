package it.antonio.sentiment;

public class SentimentRNNResult {
	private double unbiased; 
	private double positive; 
	private double negative; 
	private double mixedFeelings;
	
	private int notFoundInWordVec;
	
	
	public SentimentRNNResult(double unbiased, double positive, double negative, double mixedFeelings,
			int notFoundInWordVec) {
		super();
		this.unbiased = unbiased;
		this.positive = positive;
		this.negative = negative;
		this.mixedFeelings = mixedFeelings;
		this.notFoundInWordVec = notFoundInWordVec;
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
	
	
}
