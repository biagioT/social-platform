package it.antonio.nlp;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class SentimentLexiconTest {

	@Test
	void test() {
		SentimentLexicon sl = SentimentLexicon.create();
		
		
		System.out.println(sl.sentiment("non").positive + " " + sl.sentiment("non").negative + " " + sl.sentiment("non").polarity + " " + sl.sentiment("non").intensity);
		
		System.out.println(sl.sentiment("bello").positive + " " + sl.sentiment("bello").negative);
		
		Assert.assertTrue(sl.sentiment("bello").positive > sl.sentiment("bello").negative);
		Assert.assertTrue(sl.sentiment("brutto").positive < sl.sentiment("brutto").negative);
		
	}

}
