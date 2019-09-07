package it.antonio.nlp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class PosTaggerTest {

	@Test
	void test() {
		PosTagger pos = PosTagger.create();
		
		String[] test = "il mondo è molto Complesso".split(" ");
		String[] output = pos.tag(test);
		
		System.out.println(Arrays.asList(output));
		
		output = pos.tagSimple(test);
		
		System.out.println(Arrays.asList(output));
		
		
		System.out.println(Arrays.asList(pos.getPosTags()));
		System.out.println(Arrays.asList(pos.getPosTagsSimple()));
	}

}
