package it.antonio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.rdd.api.java.JavaMongoRDD;

import it.antonio.nlp.commons.SentimentRNNResult;
import it.antonio.nlp.commons.Token.Sentiment;

public class TwitterSentiment implements ZeppelinExecutor {
	
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {

		SparkConf sparkConf = new SparkConf();
		sparkConf.setAppName("Twitter to Mongo");

		sparkConf.setMaster("local[*]");
		sparkConf.set("spark.mongodb.input.uri", "mongodb://bigdata:pizza001@164.68.123.164/bigdata.twitter");
		sparkConf.set("spark.mongodb.output.uri", "mongodb://bigdata:pizza001@164.68.123.164/bigdata.twitter");
		
		SparkContext sc = new SparkContext(sparkConf);

		SparkSession sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();
		SQLContext sqlcontext = new SQLContext(sparkSession);

		TwitterSentiment tw = new TwitterSentiment();
		tw.execute(sc, sqlcontext);

		Dataset<Row> sqlDF = sqlcontext.sql("SELECT * FROM sentiment");
		sqlDF.show(false);

	}

	public String word="bologna"; 
	
	
	
	
	@Override
	public void execute(SparkContext sc, SQLContext sqlContext) {
		
		
		
		JavaSparkContext jsc = new JavaSparkContext(sc);

		JavaMongoRDD<Document> rdd = MongoSpark.load(jsc);

		// filtered on mongo before load to spark
		LocalDateTime dateTime = LocalDateTime.now().minusHours(3);

		String isoDate = DateTimeFormatter.ISO_DATE_TIME.format(dateTime);
		Document mongoQuery = Document.parse("{ $match: { createdAt : { $gte : \"" + isoDate + "\" } } }");

		JavaMongoRDD<Document> filteredDocuments = rdd.withPipeline(Collections.singletonList(mongoQuery));

		JavaRDD<String> lines = filteredDocuments.map(d -> d.getString("text")).filter(text-> text.toLowerCase().contains(word.toLowerCase()));

		
		JavaRDD<SentimentData> sentimentData = lines.map(t -> {
			
			
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://localhost:8080/sentiment");
			post.setEntity(new StringEntity(t));
			HttpResponse output = client.execute(post);
			
			if(output.getStatusLine().getStatusCode() != 200) {
				throw new IllegalStateException(output.getStatusLine().getStatusCode() +  " " + output.getStatusLine().getReasonPhrase());
			}
			
			InputStream content = output.getEntity().getContent();
			
			Gson gson = new Gson();
			SentimentRNNResult result = gson.fromJson(new InputStreamReader(content), SentimentRNNResult.class);
			SentimentData data = new SentimentData();
			
			data.text = t;
			data.positive = result.getPositive();
			data.negative = result.getNegative();
			data.unbiased = result.getUnbiased();
			data.mixedFeelings = result.getMixedFeelings();
			data.notFoundInWordVec = result.getNotFoundInWordVec();		

			return data;
		});

		Dataset<Row> df = sqlContext.createDataFrame(sentimentData, SentimentData.class);

		df.createOrReplaceTempView("sentiment");
	}

	public static class SentimentData {
		private double unbiased;
		private double positive;
		private double negative;
		private double mixedFeelings;
		private String text;
		private int notFoundInWordVec;
		
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

	}

}
