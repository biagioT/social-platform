package it.antonio.social;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.spark.MongoSpark;

import twitter4j.Status;
import twitter4j.auth.Authorization;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterCollectorMongo {

	public static Gson gson;
	
	
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		gson = gsonBuilder.create();
	}
	
	
	public static void main(String... args) throws InterruptedException, IOException {

		Logger.getRootLogger().setLevel(Level.ERROR);

		try {
			
			SparkConf sparkConf = new SparkConf();
			sparkConf.setAppName("Twitter to Mongo");
			//sparkConf.setMaster("local[2]");
			
			sparkConf.set("spark.mongodb.input.uri", "mongodb://bigdata:pizza001@164.68.123.164/bigdata.twitter");
			sparkConf.set("spark.mongodb.output.uri", "mongodb://bigdata:pizza001@164.68.123.164/bigdata.twitter");

			JavaStreamingContext streamingContext = new JavaStreamingContext(sparkConf, Durations.seconds(3));

			final Configuration conf = new ConfigurationBuilder().setDebugEnabled(true)
					.setOAuthConsumerKey("1OKXstn92fYjv8l1fgMRasScs")
					.setOAuthConsumerSecret("9O272OqMBxI6cuaK3XvKICyRXEWboR19ksyYVbQtXuCrqxAkXb")
					.setOAuthAccessToken("18577987-ny9PYtszs172ntg8GncNFTlxrnqnBzLG34Ajz2OgV")
					.setOAuthAccessTokenSecret("UbczOyt2FFCcOLFIxgtQeMiLitUTLJGj80lb5AWLKnss7").build();

			final Authorization twitterAuth = new OAuthAuthorization(conf);

			SocialDataReceiver receiver = new SocialDataReceiver(twitterAuth);
			
			receiver.addTwitterQuery("bologna", "roma", "bologna");
			receiver.addTwitterQuery("sample");
			
			JavaReceiverInputDStream<SocialData> stream =streamingContext.receiverStream(receiver);
			
			JavaDStream<Status> tweets = stream.filter(data -> data.typeOf(Status.class))
												.map(data -> data.getValue(Status.class))
												.filter(tweet-> "it".equalsIgnoreCase(tweet.getLang()));
												
			JavaDStream<Document> tweetsJson = tweets.map(tweet -> {
				
				String twitterJSON = gson.toJson(tweet);

				// Document document = new Document();
				// document.put("text", t.getText());
				return Document.parse(twitterJSON);
			});
			
			tweetsJson.foreachRDD(jsonTweet -> {
				
				MongoSpark.save(jsonTweet);
				
			});
			

			streamingContext.start();
			streamingContext.awaitTermination();

			streamingContext.close();
		} catch (Throwable t) {

			t.printStackTrace();
		}

	}

}
