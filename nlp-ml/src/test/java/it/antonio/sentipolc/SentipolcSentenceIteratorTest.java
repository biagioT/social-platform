package it.antonio.sentipolc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import it.antonio.tweet.SentipolcSentenceIterator;
import it.antonio.tweet.TweetData;

class SentipolcSentenceIteratorTest {

	@Test
	void test() throws FileNotFoundException, IOException {
		// File input = new
		// File("/home/antonio/priv/bigdata/SentimentTest/src/main/resources/training_set_sentipolc16.csv");
		File input = new File(
				"/home/antonio/priv/bigdata/SentimentTest/src/main/resources/test_set_sentipolc16_gold2000.csv");

		SentipolcSentenceIterator it = new SentipolcSentenceIterator(new BufferedReader(new FileReader(input)));

		
		while (it.hasNext()) {
			TweetData t = it.next();
			System.out.println(t);
		
		}
		
		

	}

}
