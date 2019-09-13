package it.antonio.nlp.commons;

public class Token {
	public final String word;
	public final String lemma;
	public final String posTag;
	public final String nerTag;
	public final String specialTokenType;
	public final Sentiment sentiment;

	public Token(String word, String lemma, String nerTag, String posTag, String specialTokenType,
			Sentiment sentiment) {
		super();
		this.word = word;
		this.lemma = lemma;
		this.nerTag = nerTag;
		this.posTag = posTag;
		this.specialTokenType = specialTokenType;
		this.sentiment = sentiment;
	}

	public static class Sentiment {
		public double positive;
		public double negative;
		public double polarity;
		public double intensity;

		public Sentiment(double positive, double negative, double polarity, double intensity) {
			super();
			this.positive = positive;
			this.negative = negative;
			this.polarity = polarity;
			this.intensity = intensity;
		}

		@Override
		public String toString() {
			return positive > negative ? "positive" : "negative";
		}

	}
}