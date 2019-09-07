package it.antonio.sentiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import it.antonio.nlp.NLPPipeline;
import it.antonio.nlp.SentimentLexicon;
import it.antonio.nlp.SentimentLexicon.Sentiment;
import it.antonio.nlp.dl4j.NLPTokenizerFactory;

public class SentimentRNNTest implements Constants {

	

	public static void main(String... args) throws IOException, URISyntaxException {

		File wor2vecModel = new File(FILE_WORD2VEC);

		Word2Vec vec = WordVectorSerializer.readWord2VecModel(wor2vecModel);

		File netModel = new File(FILE_NETWORK);

		MultiLayerNetwork net = ModelSerializer.restoreMultiLayerNetwork(netModel);

		NLPTokenizerFactory tokenizerFactory = new NLPTokenizerFactory(NLPPipeline.create());

		SentimentLexicon lex = SentimentLexicon.create();

		while (true) {

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			System.out.print("text: ");

			String text = br.readLine().trim();

			List<String> tokens = tokenizerFactory.create(text).getTokens();
			System.out.println(tokens);

			INDArray features = Nd4j.zeros(1, WORD_SIZE, tokens.size());

			double ok = 0;
			for (int j = 0; j < tokens.size(); j++) {

				String word = tokens.get(j);
				INDArray wordVector = vec.getWordVectorMatrixNormalized(word);// .mul(10000);
				// features.put(new INDArrayIndex[]{NDArrayIndex.point(0), NDArrayIndex.all(),
				// NDArrayIndex.point(j)}, wordVector);

				features.put(new INDArrayIndex[] { NDArrayIndex.point(0), NDArrayIndex.interval(0, 100),
						NDArrayIndex.point(j) }, wordVector);

				/*
				Sentiment sentiment = lex.sentiment(word);
				if(sentiment != null) {
					
					features.putScalar(new int[] {0, 100, j},  sentiment.positive);
					features.putScalar(new int[] {0, 101, j},  sentiment.negative);
				}
				*/

			}

			INDArray output = net.output(features);
			long timeSeriesLength = output.size(2);
			INDArray probabilitiesAtLastWord = output.get(NDArrayIndex.point(0), NDArrayIndex.all(),
					NDArrayIndex.point(timeSeriesLength - 1));
			
			
			System.out.println("unbiased: " + probabilitiesAtLastWord.getDouble(0));
			System.out.println("positive: " + probabilitiesAtLastWord.getDouble(1));
			System.out.println("negative: " + probabilitiesAtLastWord.getDouble(2));
			System.out.println("mixed feelings: " + probabilitiesAtLastWord.getDouble(3));
			
		}

	}

}
