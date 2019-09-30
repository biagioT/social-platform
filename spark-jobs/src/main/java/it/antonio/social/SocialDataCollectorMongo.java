package it.antonio.social;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import com.mongodb.spark.config.WriteConfig;

import it.antonio.social.receiver.SocialDataReceiver;
import net.dean.jraw.models.Comment;
import twitter4j.Status;

public class SocialDataCollectorMongo {

	public static Gson gson;
	
	
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		gson = gsonBuilder.create();
	}
	
	
	public static void main(String... args) throws InterruptedException, IOException {

		try {
			
			SparkConf sparkConf = new SparkConf();
			
			sparkConf.setAppName("Social to Mongo");
			sparkConf.setMaster("local[2]");
			
			sparkConf.set("spark.mongodb.output.uri", "mongodb://bigdata:pizza001@164.68.123.164/bigdata.unused_collection");
			
			sparkConf.set("spark.social.twitter.oauth.consumer.key", "1OKXstn92fYjv8l1fgMRasScs");
			sparkConf.set("spark.social.twitter.oauth.consumer.secret", "9O272OqMBxI6cuaK3XvKICyRXEWboR19ksyYVbQtXuCrqxAkXb");
			sparkConf.set("spark.social.twitter.oauth.access.token", "18577987-ny9PYtszs172ntg8GncNFTlxrnqnBzLG34Ajz2OgV");
			sparkConf.set("spark.social.twitter.oauth.access.tokensecret", "UbczOyt2FFCcOLFIxgtQeMiLitUTLJGj80lb5AWLKnss7");
			
			sparkConf.set("spark.social.twitter.query", "bologna, roma,milano,pd,lega,m5s,partito,lega,movimento,attualità,traffico,notizia,notizie,news,bello,brutto,nuovo,emilia,romagna,umbria,elezioni,elezione,salvini,matteo,periferia,xm24");
			sparkConf.set("spark.social.twitter.mongodb.collection", "twitter-test");

			
			sparkConf.set("spark.social.reddit.oauth.user", "ilpizze");
			sparkConf.set("spark.social.reddit.oauth.password", "p12124545");
			sparkConf.set("spark.social.reddit.oauth.clientid", "YBlWgMmCpNGGxQ");
			sparkConf.set("spark.social.reddit.oauth.clientsecret", "IDxAHgTKRtdD9Ds2300JUzjRZ94");
			
			sparkConf.set("spark.social.reddit.subreddits", "italy, Bologna,Italia");
			sparkConf.set("spark.social.reddit.mongodb.collection", "reddit-test");
			
			JavaStreamingContext streamingContext = new JavaStreamingContext(sparkConf, Durations.seconds(3));
			
			Map<String, String> twitterOverrides = new HashMap<String, String>();
			twitterOverrides.put("collection", sparkConf.get("spark.social.twitter.mongodb.collection"));
			twitterOverrides.put("writeConcern.w", "majority");
			WriteConfig twitterWriteConfig = WriteConfig.create(streamingContext.sparkContext()).withOptions(twitterOverrides);

			Map<String, String> redditOverrides = new HashMap<String, String>();
			redditOverrides.put("collection",sparkConf.get("spark.social.reddit.mongodb.collection"));
			redditOverrides.put("writeConcern.w", "majority");
			WriteConfig redditWriteConfig = WriteConfig.create(streamingContext.sparkContext()).withOptions(redditOverrides);


			SocialDataReceiver receiver = new SocialDataReceiver(sparkConf);
			
			
			Logger.getRootLogger().setLevel(Level.ERROR);

			JavaReceiverInputDStream<SocialData> stream =streamingContext.receiverStream(receiver);
			
			JavaDStream<Status> tweets = stream.filter(data -> data.typeOf(Status.class))
												.map(data -> data.getValue(Status.class))
												.filter(tweet-> "it".equalsIgnoreCase(tweet.getLang()));
												
			JavaDStream<Document> tweetsJson = tweets.map(tweet -> {
				
				String twitterJSON = gson.toJson(tweet);
				return Document.parse(twitterJSON);
			});
			
			tweetsJson.foreachRDD(jsonTweet -> {
				
				
			    MongoSpark.save(jsonTweet, twitterWriteConfig );
				
			});
			
			JavaDStream<Comment> redditComments =  stream.filter(data -> data.typeOf(Comment.class))
															.map(data -> data.getValue(Comment.class));
			
			
			JavaDStream<Document> redditCommentsJson = redditComments.map(tweet -> {
				
				String twitterJSON = gson.toJson(tweet);
				return Document.parse(twitterJSON);
			});
			
			redditCommentsJson.foreachRDD(redditCommentRDD -> {
				
				MongoSpark.save(redditCommentRDD, redditWriteConfig );
				
				
			});

			streamingContext.start();
			streamingContext.awaitTermination();

			streamingContext.close();
		} catch (Throwable t) {

			t.printStackTrace();
		}

	}

}
