package it.antonio.nlp;

import org.junit.jupiter.api.Test;

class MorphitLemmatizerTest {

	@Test
	void test() {
		MorphitLemmatizer m = MorphitLemmatizer.create();
		
		
		System.out.println(m.lemma("mangiassi"));
	}

}
