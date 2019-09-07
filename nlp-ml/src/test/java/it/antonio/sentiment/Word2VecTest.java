package it.antonio.sentiment;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import it.antonio.sentiment.Constants;

class Word2VecTest implements Constants{

	@Test
	void test() {
		File wor2vecModel = new File(FILE_WORD2VEC);

		Word2Vec vec = WordVectorSerializer.readWord2VecModel(wor2vecModel);
		
		System.out.println(vec.wordsNearest("casa", 7));
		System.out.println(vec.wordsNearest("pera", 7));
		System.out.println(vec.wordsNearest("spritz", 7));

		VocabCache<VocabWord> cache = vec.getLookupTable().getVocabCache();
		System.out.println(cache.containsWord("casa"));
		System.out.println(cache.containsWord("mangiai"));
		
		INDArray v1 = vec.getWordVectorMatrix("casa");
		INDArray v2 = vec.getWordVectorMatrixNormalized("casa");
		
		double[] dv1 = v1.toDoubleVector();
		
		INDArray rv1 = Nd4j.createFromArray(dv1);
		System.out.println(v1);
		System.out.println(v2);
		System.out.println(dv1);
		System.out.println(rv1);
		System.out.println(rv1.equals(v1));

		
	}

}
