package it.antonio.social;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.Authorization;

public class SocialDataReceiver extends Receiver<SocialData> {

	private static final long serialVersionUID = 374517035410672666L;

	private volatile Authorization twitterAuth;
	private volatile List<String[]> queries = new ArrayList<>();
	private volatile List<TwitterStream> openTwitterStreams = new ArrayList<>();

	public SocialDataReceiver(Authorization twitterAuth) {
		super(StorageLevel.MEMORY_AND_DISK_2());
		this.twitterAuth = twitterAuth;
	}

	public void addTwitterQuery(String... query) {
		this.queries.add(query);
	}
	
	@Override
	public void onStart() {
		queries.forEach(query-> {
			startTwitterStream(query);
			try {
				Thread.sleep(5000);
				// per rispettare la rate limit
			} catch (InterruptedException e) {
				Logger.getRootLogger().error("Errore nel recupero del tweet", e);
				
			}
			
		});

	
	}

	@Override
	public void onStop() {
		openTwitterStreams.forEach(ts -> ts.shutdown());
		openTwitterStreams.clear();
	}

	public void startTwitterStream(String[] query) {
		try {
			TwitterStream newTwitterStream = new TwitterStreamFactory().getInstance(twitterAuth);
			newTwitterStream.addListener(new StatusListener() {

				@Override
				public void onException(Exception ex) {
					Logger.getRootLogger().error("Errore nel recupero del tweet", ex);
					
				}

				@Override
				public void onStatus(Status status) {
					store(new SocialData(status));
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
				newTwitterStream.sample("it");
			} else {
				newTwitterStream.filter(query);
			}
			
			this.openTwitterStreams.add(newTwitterStream);
			
		} catch (Exception e) {
			Logger.getRootLogger().error("Errore nella creazione dello stream", e);
			
			
		}
	}

}