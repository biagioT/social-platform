package it.antonio.social.receiver;

import org.apache.spark.SparkConf;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

import it.antonio.social.SocialData;

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