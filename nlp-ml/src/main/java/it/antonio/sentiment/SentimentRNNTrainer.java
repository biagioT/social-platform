package it.antonio.sentiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.conf.layers.SelfAttentionLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import it.antonio.nlp.NLPPipeline;
import it.antonio.nlp.SentimentLexicon;
import it.antonio.tweet.SentipolcSentenceIterator;
import it.antonio.tweet.TweetData;

public class SentimentRNNTrainer implements Constants {
	
	
	public static void main(String...args) throws IOException, URISyntaxException {
		
		
		UIServer uiServer = UIServer.getInstance();

	    //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
	    StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later

	    //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
	    uiServer.attach(statsStorage);

	    Runtime.getRuntime().addShutdownHook(new Thread() {
	        public void run() {
	        	uiServer.stop();
	        }
	    });
		
		File csv1 = new File(SentimentRNNTrainer.class.getClassLoader().getResource("training_set_sentipolc16.csv").toURI());
		SentipolcSentenceIterator it = new SentipolcSentenceIterator(new BufferedReader(new FileReader(csv1)));

		List<TweetData> tweets = new ArrayList<>(); 
		it.forEachRemaining(tweets::add);
		
		File csv2 = new File(SentimentRNNTrainer.class.getClassLoader().getResource("test_set_sentipolc16_gold2000.csv").toURI());
		it = new SentipolcSentenceIterator(new BufferedReader(new FileReader(csv2)));
		it.forEachRemaining(tweets::add);
		
		
		
		NLPTokenizerFactory tokenizerFactory = new NLPTokenizerFactory(NLPPipeline.create());
		
		File wor2vecModel = new File(FILE_WORD2VEC);
		if(!wor2vecModel.exists()) {
			//Word2VecTrainer.train(tweets.stream().map(t-> t.text).collect(Collectors.toList()), tweets.size(), wor2vecModel);
			
			throw new IllegalStateException("Word2vec model not trained");
		}
		
		Word2Vec vec = WordVectorSerializer.readWord2VecModel( wor2vecModel);
		
		SentimentLexicon lex = SentimentLexicon.create();
		//VocabCache<VocabWord> cache = vec.getVocab();
		//int outputSize = cache.numWords(); 
		
		
		
		long maxTweetSize = -1;
		for(TweetData tweet: tweets) {
			List<String> tokens = tokenizerFactory.create(tweet.text).getTokens();
			if(tokens.size() > maxTweetSize) {
				maxTweetSize = tokens.size();
			}
		}
		
		List<DataSet> tweetDataSetList = new ArrayList<>();

		for(TweetData tweet: tweets) {
			
			List<String> tokens = tokenizerFactory.create(tweet.text).getTokens();
			
			INDArray features = Nd4j.zeros(1, WORD_SIZE, maxTweetSize);
			INDArray labels = Nd4j.zeros(1, NUM_LABEL, maxTweetSize);
			
			INDArray featuresMask = Nd4j.zeros( 1, maxTweetSize);
			featuresMask.get(new INDArrayIndex[] {NDArrayIndex.point(0), NDArrayIndex.interval(0, tokens.size())}).assign(1);

			INDArray labelsMask = Nd4j.zeros( 1, maxTweetSize);
			
			for (int j = 0; j < tokens.size(); j++) {


				String word = tokens.get(j);
				
				
				INDArray wordVector = vec.getWordVectorMatrixNormalized(word);//.mul(10000);
				
				features.put(new INDArrayIndex[]{NDArrayIndex.point(0), NDArrayIndex.interval(0, 100), NDArrayIndex.point(j)}, wordVector);
				/*
				Sentiment sentiment = lex.sentiment(word);
				if(sentiment != null) {
					
					features.putScalar(new int[] {0, 100, j},  sentiment.positive);
					features.putScalar(new int[] {0, 101, j},  sentiment.negative);
				}
				*/
				
				featuresMask.put(new INDArrayIndex[]{NDArrayIndex.point(0), NDArrayIndex.point(j)}, 1);
				
				if(j == tokens.size() -1) {
				
					
					int index = 0; 
					
					if(!tweet.positive && !tweet.negative)  index = 0; // unbiased
					if(tweet.positive && !tweet.negative)  index = 1; // positive
					if(!tweet.positive && tweet.negative)  index = 2; // negative
					if(tweet.positive && tweet.negative)  index = 3; // mixed feelings
		            
					
					labels.putScalar(new int[]{0,index,j}, 1);
					
					labelsMask.put(new INDArrayIndex[]{NDArrayIndex.point(0), NDArrayIndex.point(j)}, 1);
					
				}
				
				
			}
				
			DataSet dataset = new DataSet(features, labels, featuresMask, labelsMask);
			tweetDataSetList.add(dataset);
			
		}
		
		
		
		//DataSetIterator trainingData = new ListDataSetIterator<>(tweetDataSetList,1);
		
		  
				
		MultiLayerNetwork net;
		File netModel = new File(FILE_NETWORK);
		File networkStats = new File(FILE_NETWORK_STATS);
		
		long currentEpoch = 0;
		double minError = 100;
		if(!netModel.exists()) {
			MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
					.seed(123)
					.updater(new Adam(0.005))
		            .l2(1e-5)

					//.updater(new RmsProp(0.1))
					.weightInit(WeightInit.XAVIER)
					.gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
					.gradientNormalizationThreshold(1.0)

					.list()
					.layer(new LSTM.Builder().nIn(WORD_SIZE ).nOut(HIDDEN_LAYER_SIZE)
							.activation(Activation.TANH)
							.build())
					
					.layer(
                            new SelfAttentionLayer.Builder().nIn( /* num layers precedenti* layer size*/ HIDDEN_LAYER_SIZE).nOut(HIDDEN_LAYER_SIZE).nHeads(1).projectInput(true).build())
                    
					//.layer(new GlobalPoolingLayer.Builder().poolingType(PoolingType.MAX).build())
					.layer(new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
							.lossFunction(LossFunctions.LossFunction.MCXENT)
							.nIn(HIDDEN_LAYER_SIZE).nOut(NUM_LABEL).build())
					.build();

			net = new MultiLayerNetwork(conf);
			net.init();
	        // net.setListeners(new ScoreIterationListener(1));
	   	
		} else {
			net = ModelSerializer.restoreMultiLayerNetwork(netModel);
			if(networkStats.exists()) {
				BufferedReader statsReader = new BufferedReader(new FileReader(networkStats));
				currentEpoch = Long.parseLong(statsReader.readLine());
				minError = Double.parseDouble(statsReader.readLine());
				statsReader.close();
			}
			
			
			
		}
	    net.setListeners(new StatsListener(statsStorage));
			
		int testSetSize = (int) (tweetDataSetList.size() * 0.1);
		int trainingSetSize = (int) (tweetDataSetList.size() * 0.9);
		
		
		for (int epoch = 0; epoch < 5000; epoch++) {

			Collections.shuffle(tweetDataSetList);

			DataSetIterator trainingData = new ListDataSetIterator<>(tweetDataSetList.subList(0, trainingSetSize));

			net.fit(trainingData);

			
						
			// ERROR CALCULATION
			List<TweetData> testTweets = tweets.subList(trainingSetSize, testSetSize + trainingSetSize); 
			
			
			double error = 0;
			for(TweetData tweet: testTweets) {
				
				List<String> tokens = tokenizerFactory.create(tweet.text).getTokens();
				//net.rnnClearPreviousState();
				
				INDArray features = Nd4j.zeros(1, WORD_SIZE , tokens.size());
				
				double ok = 0;
				for (int j = 0; j < tokens.size(); j++) {

					String word = tokens.get(j);
					INDArray wordVector = vec.getWordVectorMatrixNormalized(word);//.mul(10000);
					//features.put(new INDArrayIndex[]{NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(j)}, wordVector);

					features.put(new INDArrayIndex[]{NDArrayIndex.point(0), NDArrayIndex.interval(0, 100), NDArrayIndex.point(j)}, wordVector);
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
                
                int predictedIndex = Nd4j.getExecutioner().exec(new IMax(probabilitiesAtLastWord, 0)).getInt(0);
		        
                int index = 0; 
				
				if(!tweet.positive && !tweet.negative)  index = 0; // unbiased
				if(tweet.positive && !tweet.negative)  index = 1; // positive
				if(!tweet.positive && tweet.negative)  index = 2; // negative
				if(tweet.positive && tweet.negative)  index = 3; // mixed feelings
				
				if(index == predictedIndex) {
					ok++;
				}
				
				
				
				error += 1 - ok;
	
			}
			
			error = error /tweets.size();
			
		

			System.out.println("Error:  " + Math.round(error * 100 * 100.0) / 100.0+ "% - from evaluation");
			Evaluation evaluatiion = net.evaluate(trainingData);
			System.out.println("Accuracy " + evaluatiion.accuracy());
			System.out.println("*******************************************************");
			
			if(error < minError) {
				currentEpoch += epoch;
				minError = error;
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(networkStats, false));
				writer.write(currentEpoch + "\n");
				writer.write(minError + "\n");
				writer.flush();
				writer.close();
				
				ModelSerializer.writeModel(net, netModel, true);
			}
			

			if(error < 0.001 && evaluatiion.accuracy() > 0.93) {
				System.out.println("Epoch " + epoch);
				break;
			}

		}
		
		UIServer.getInstance().stop();
	}


	
}
