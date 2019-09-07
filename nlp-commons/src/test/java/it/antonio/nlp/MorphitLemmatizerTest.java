package it.antonio.nlp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MorphitLemmatizerTest {

	@Test
	void test() {
		MorphitLemmatizer m = MorphitLemmatizer.create();
		
		
		System.out.println(m.lemma("mangiassi"));
	}

}
