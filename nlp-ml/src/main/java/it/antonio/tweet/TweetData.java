package it.antonio.tweet;

public class TweetData {
	public String text;
	public boolean positive;
	public boolean negative;

	public TweetData(String text, boolean positive, boolean negative) {
		super();
		this.text = text;
		this.positive = positive;
		this.negative = negative;
	}

	@Override
	public String toString() {
		return "TweetData [positive=" + positive + ", negative=" + negative + ", text=" + text + "]";
	}

}