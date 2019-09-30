package it.antonio.social.receiver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.storage.StorageLevel;
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

public class SocialDataReceiver extends Receiver<SocialData> {

	private static final long serialVersionUID = 374517035410672666L;

	private TwitterThread twitterThread;
	private RedditThread redditThread;
	
	public SocialDataReceiver(SparkConf sparkConf) {
		super(StorageLevel.MEMORY_AND_DISK_2());
		
		// twitter
		twitterThread = new TwitterThread(sparkConf, this);
		redditThread = new RedditThread(sparkConf, this);
		
	}

	
	
	@Override
	public void onStart() {
		twitterThread.start();
		redditThread.start();
	}

	@Override
	public void onStop() {
		twitterThread.onStop();
	}

	
	
	
	
	
	

}