package it.antonio.nlp;

import org.junit.jupiter.api.Test;

class StopWordsTest {

	@Test
	void test() {
		StopWords sw = StopWords.create();
		
		System.out.println(sw.isStopWord("non"));
		System.out.println(sw.isStopWord("casa"));
		System.out.println(sw.isStopWord("e"));
		System.out.println(sw.isStopWord("mela"));
		System.out.println(sw.isStopWord("il"));
	}

}
