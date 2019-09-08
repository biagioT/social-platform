package it.antonio.sentiment;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import it.antonio.nlp.NLPPipeline;
import it.antonio.nlp.dl4j.NLPTokenizerFactory;

public class SentimentRNN implements Constants, Serializable{

	private static final long serialVersionUID = 1L;
	private MultiLayerNetwork net;
	private NLPTokenizerFactory tokenizerFactory;
	private Word2Vec vec;
	

	public SentimentRNN(MultiLayerNetwork net, NLPTokenizerFactory tokenizerFactory, Word2Vec vec) {
		super();
		this.net = net;
		this.tokenizerFactory = tokenizerFactory;
		this.vec = vec;
	}


	public SentimentRNNResult sentiment(String text) {
		List<String> tokenized = tokenizerFactory.create(text).getTokens();
		
		
		System.out.println(tokenized);
		List<INDArray> wordVectors = new ArrayList<INDArray>();
		
		tokenized.forEach(token -> {
			INDArray wordVector = vec.getWordVectorMatrixNormalized(token);// .mul(10000);
			if(wordVector != null ) {
				wordVectors.add(wordVector);
			}
		});
		
		
		INDArray features = Nd4j.zeros(1, WORD_SIZE, wordVectors.size());

		for (int j = 0; j < wordVectors.size(); j++) {

			
			INDArray wordVector = wordVectors.get(j);

			features.put(new INDArrayIndex[] { NDArrayIndex.point(0), NDArrayIndex.interval(0, 100), NDArrayIndex.point(j) }, wordVector);

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
		INDArray probabilitiesAtLastWord = output.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1));
		
		double unbiased = probabilitiesAtLastWord.getDouble(0);
		double positive = probabilitiesAtLastWord.getDouble(1);
		double negative = probabilitiesAtLastWord.getDouble(2);
		double mixedFeelings = probabilitiesAtLastWord.getDouble(3);
		
		int notFoundInWordVec = tokenized.size() - wordVectors.size();
		
		return new SentimentRNNResult(unbiased, positive, negative, mixedFeelings, notFoundInWordVec);
		
	}


	public static SentimentRNN create(File wor2vecModel, File netModel) throws IOException, URISyntaxException {


		NLPTokenizerFactory tokenizerFactory = new NLPTokenizerFactory(NLPPipeline.create());
		
		Word2Vec vec = WordVectorSerializer.readWord2VecModel(wor2vecModel);

		MultiLayerNetwork net = ModelSerializer.restoreMultiLayerNetwork(netModel);

		
		return new SentimentRNN(net, tokenizerFactory, vec);
	}
		
}
