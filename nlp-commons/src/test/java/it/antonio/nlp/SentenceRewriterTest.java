package it.antonio.nlp;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SentenceRewriterTest {

	@Test
	void test() {
		 SentenceRewriter sentenceRewriter = new SentenceRewriter();
	     NLPPipeline pipeline = NLPPipeline.create();
		 
		 	
		 List<String> rewrited = sentenceRewriter.rewrite(pipeline.tokens("Il mio nome è Antonio"));
		 
		 System.out.println(rewrited);
		 Assertions.assertEquals(rewrited, Arrays.asList("il" , "mio", "nome", "essere", "I-PER"));
		 
		 
		 rewrited = sentenceRewriter.rewrite(pipeline.tokens("#AntonacciBiagio di fronte all'ippodromo Le Cappanelle a Roma"));
		 System.out.println(rewrited);
		 Assertions.assertEquals(rewrited, Arrays.asList("hashtag", "di", "fronte", "all'", "ippodromo", "le", "I-LOC", "a", "I-LOC"));
			
		 //String[] expected = new String[] {"il", "mio", "nome", "essere", "I-PER"};
		 
		 //Assert.assertArrayEquals(expected, rewrited);
	}

}
