package it.antonio.social.receiver;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.receiver.Receiver;

import it.antonio.social.SocialData;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.Authorization;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterThread extends Thread implements Serializable{
		
		private static final long serialVersionUID = 1L;

		private volatile Authorization twitterAuth;

		private TwitterStream openStream;
		private String[] query;

		private SocialDataReceiver receiver;
		
		
		public TwitterThread(SparkConf sparkConf, SocialDataReceiver receiver) {
			super();
		
			this.receiver = receiver;
			
			final Configuration conf = new ConfigurationBuilder().setDebugEnabled(true)
					.setOAuthConsumerKey(sparkConf.get("spark.social.twitter.oauth.consumer.key"))
					.setOAuthConsumerSecret(sparkConf.get("spark.social.twitter.oauth.consumer.secret"))
					.setOAuthAccessToken(sparkConf.get("spark.social.twitter.oauth.access.token"))
					.setOAuthAccessTokenSecret(sparkConf.get("spark.social.twitter.oauth.access.tokensecret")).build();
			this.twitterAuth = new OAuthAuthorization(conf);
			
			
			// stream molteplici non supportati dall'api (fino a 400 parole)
			String queryString = sparkConf.get("spark.social.twitter.query");
			query = Arrays.asList(queryString.split(",")).stream().map(String::trim).toArray(l -> new String[l]);
			
		}

		@Override
		public void run() {
			try {
				if(openStream != null) {
					openStream.shutdown();
				}
				
				openStream = new TwitterStreamFactory().getInstance(twitterAuth);
				openStream.addListener(new StatusListener() {

					@Override
					public void onException(Exception ex) {
						Logger.getRootLogger().error("Errore nel recupero del tweet", ex);
						
					}

					@Override
					public void onStatus(Status status) {
						receiver.store(new SocialData(status));
					}

					@Override
					public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

					@Override
					public void onStallWarning(StallWarning warning) {}

					@Override
					public void onScrubGeo(long userId, long upToStatusId) {}

					@Override
					public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
				});

				if (query.length == 1 && "sample".equalsIgnoreCase(query[0])) {
					openStream.sample("it");
				} else {
					openStream.filter(query);
				}
				
				
			} catch (Exception e) {
				Logger.getRootLogger().error("Errore nella creazione dello stream", e);
			}
		}
		
		
		
		public void onStop() {
			openStream.shutdown();
		}
	}
	
	
	