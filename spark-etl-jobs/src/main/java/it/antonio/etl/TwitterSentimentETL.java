package it.antonio.etl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.config.ReadConfig;
import com.mongodb.spark.rdd.api.java.JavaMongoRDD;

import it.antonio.nlp.NLPPipeline.Token;
import it.antonio.sentiment.Constants;
import it.antonio.sentiment.SentimentRNN;
import it.antonio.sentiment.SentimentRNNResult;

public class TwitterSentimentETL {
	
	public static SentimentRNN rnn;
	public static Gson gson;
	
	
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		gson = gsonBuilder.create();
	}
	public static void main(String[] args) throws Exception {

		
		
		SparkConf sparkConf = new SparkConf();
		sparkConf.setAppName("Twitter to Mongo");

		sparkConf.setMaster("local[*]");
		sparkConf.set("spark.mongodb.input.uri", "mongodb://bigdata:pizza001@164.68.123.164/bigdata.collectionToOvveride");
		//sparkConf.set("spark.mongodb.output.uri", "mongodb://bigdata:pizza001@164.68.123.164/bigdata.twitter");
		sparkConf.set("spark.sentiment.word2vec.file", Constants.FILE_WORD2VEC);
		sparkConf.set("spark.sentiment.neturalnet.file", Constants.FILE_NETWORK);

		SparkContext sc = new SparkContext(sparkConf);

		createSentimentRnn(sc);
		
		//SparkSession sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();
		//SQLContext sqlcontext = new SQLContext(sparkSession);

		

		JavaSparkContext jsc = new JavaSparkContext(sc);

		Map<String, String> mongoReadOptions = new HashMap<String, String>();
		mongoReadOptions.put("collection", "twitter");
	    ReadConfig readConfig = ReadConfig.create(jsc).withOptions(mongoReadOptions);
		
	    
	    
		JavaMongoRDD<Document> rdd = MongoSpark.load(jsc, readConfig );
		LocalDateTime dateTime = LocalDateTime.now().minusHours(8);

		String isoDate = DateTimeFormatter.ISO_DATE_TIME.format(dateTime);
		Document mongoQuery = Document.parse("{ $match: { createdAt : { $gte : \"" + isoDate + "\" } } }");

		JavaMongoRDD<Document> filteredDocuments = rdd.withPipeline(Collections.singletonList(mongoQuery));

		JavaRDD<SentimentData> documentAndSentiment = filteredDocuments.map(json -> {
			Long id = json.getLong("id");
			String text = json.getString("text");
			SentimentRNNResult sentiment = rnn.sentiment(text);
			
			SentimentData data = new SentimentData();
			data.setId(id);
			data.setText(text);
			data.setPositive(sentiment.getPositive());
			data.setNegative(sentiment.getNegative());
			data.setMixedFeelings(sentiment.getMixedFeelings());
			data.setUnbiased(sentiment.getUnbiased());
			data.setNotFoundInWordVec(sentiment.getNotFoundInWordVec());
			data.setTokens(sentiment.getTokens());
			
			return data;
		});
		
		JavaRDD<Document> tweetsJson = documentAndSentiment.map(data -> {
			
			String json = gson.toJson(data);

			// Document document = new Document();
			// document.put("text", t.getText());
			return Document.parse(json);
		});
		
		System.out.println(tweetsJson.count());
	    tweetsJson.take(20).forEach(System.out::println);;

		
		jsc.close();
	}
	
	
	public static void createSentimentRnn(SparkContext sc) {
		if(rnn == null) {
			synchronized (sc) {
				if(rnn == null) {
					try {
						File word2vec = new File(sc.getConf().get("spark.sentiment.word2vec.file"));
						File neuralNetwork = new File(sc.getConf().get("spark.sentiment.neturalnet.file"));
						Logger.getRootLogger().info("Loading Sentiment RNN");
						rnn = SentimentRNN.create(word2vec, neuralNetwork);
						
						Runtime runtime = Runtime.getRuntime();

						NumberFormat format = NumberFormat.getInstance();

						long maxMemory = runtime.maxMemory();
						long allocatedMemory = runtime.totalMemory();
						long freeMemory = runtime.freeMemory();

						Logger.getRootLogger().info("free memory: " + format.format(freeMemory / 1024) );
						Logger.getRootLogger().info("allocated memory: " + format.format(allocatedMemory / 1024));
						Logger.getRootLogger().info("max memory: " + format.format(maxMemory / 1024) );
						Logger.getRootLogger().info("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
					} catch (IOException | URISyntaxException e) {
						throw new RuntimeException(e);
					}
					
				}
			
			}
			
		}
	}


	public void execute(SparkContext sc, SQLContext sqlContext) {
		
		
		/*
		
		// filtered on mongo before load to spark
		
		JavaRDD<String> lines = filteredDocuments.map(d -> d.getString("text")).filter(text-> text.toLowerCase().contains(word.toLowerCase()));

		JavaRDD<SentimentData> output = lines.map(t -> {
			SentimentData data = new SentimentData();
			SentimentRNNResult result = rnn.sentiment(t);

			data.text = t;
			data.positive = result.getPositive();
			data.negative = result.getNegative();
			data.unbiased = result.getUnbiased();
			data.mixedFeelings = result.getMixedFeelings();
			data.notFoundInWordVec = result.getNotFoundInWordVec();
			return data;
		});

		Dataset<Row> df = sqlContext.createDataFrame(output, SentimentData.class);

		df.createOrReplaceTempView("sentiment");
		
		*/
	}

	public static class SentimentData {
		private double unbiased;
		private double positive;
		private double negative;
		private double mixedFeelings;
		private long id;
		private String text;
		private int notFoundInWordVec;
		private List<Token> tokens = new ArrayList<>();
		
		public double getUnbiased() {
			return unbiased;
		}

		public void setUnbiased(double unbiased) {
			this.unbiased = unbiased;
		}

		public double getPositive() {
			return positive;
		}

		public void setPositive(double positive) {
			this.positive = positive;
		}

		public double getNegative() {
			return negative;
		}

		public void setNegative(double negative) {
			this.negative = negative;
		}

		public double getMixedFeelings() {
			return mixedFeelings;
		}

		public void setMixedFeelings(double mixedFeelings) {
			this.mixedFeelings = mixedFeelings;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public int getNotFoundInWordVec() {
			return notFoundInWordVec;
		}

		public void setNotFoundInWordVec(int notFoundInWordVec) {
			this.notFoundInWordVec = notFoundInWordVec;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public List<Token> getTokens() {
			return tokens;
		}

		public void setTokens(List<Token> tokens) {
			this.tokens = tokens;
		}

		
	}

}
