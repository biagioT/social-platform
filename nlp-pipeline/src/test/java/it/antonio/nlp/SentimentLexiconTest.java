package it.antonio.nlp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SentimentLexiconTest {

	@Test
	void test() {
		SentimentLexicon sl = SentimentLexicon.create();
		
		
		System.out.println(sl.sentiment("non").positive + " " + sl.sentiment("non").negative + " " + sl.sentiment("non").polarity + " " + sl.sentiment("non").intensity);
		
		System.out.println(sl.sentiment("bello").positive + " " + sl.sentiment("bello").negative);
		
		Assertions.assertTrue(sl.sentiment("bello").positive > sl.sentiment("bello").negative);
		Assertions.assertTrue(sl.sentiment("brutto").positive < sl.sentiment("brutto").negative);
		
	}

}
