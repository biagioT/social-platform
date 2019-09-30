package it.antonio.social.receiver;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.spark.SparkConf;

import it.antonio.social.SocialData;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Listing;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.BarebonesPaginator;
import net.dean.jraw.references.SubredditReference;

public class RedditThread  extends Thread implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private SocialDataReceiver receiver;
	
	private static List<SubredditReference> subreddits = new LinkedList<SubredditReference>();
	private static AtomicInteger currentIndex = new AtomicInteger(0);
	
	public RedditThread(SparkConf sparkConf, SocialDataReceiver receiver) {
		super();
	
		this.receiver = receiver;
		
		UserAgent userAgent = new UserAgent("analyzer", "it.antonio", "v0.1", "ilpizze");

		String username = sparkConf.get("spark.social.reddit.oauth.user");
		String password = sparkConf.get("spark.social.reddit.oauth.password");
		String clientId = sparkConf.get("spark.social.reddit.oauth.clientid");
		String clientSecret = sparkConf.get("spark.social.reddit.oauth.clientsecret");
		
		Credentials credentials = Credentials.script(username, password, clientId, clientSecret);

		// This is what really sends HTTP requests
		NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);

		// Authenticate and get a RedditClient instance
		RedditClient reddit = OAuthHelper.automatic(adapter, credentials);
		reddit.setLogHttp(false);
		
		String queryString = sparkConf.get("spark.social.reddit.subreddits");
		Arrays.asList(queryString.split(",")).stream().map(String::trim).forEach(subreddit -> {
		
			subreddits.add(reddit.subreddit(subreddit));
		});
		
		
	
	}

	@Override
	public void run() {
		
		Date lastDate = new Date();
		
		while(true) {
			int index = currentIndex.get();
			SubredditReference subreddit = subreddits.get(index);
			
			currentIndex.set((index + 1 ) % subreddits.size());
			
			BarebonesPaginator<Comment> comments = subreddit.comments().limit(50).build(); // 50 records per page
			
			Iterator<Listing<Comment>> it = comments.iterator(); 
			
			LinkedList<Comment> totals = new LinkedList<Comment>();
			while(it.hasNext()) {
				
				Listing<Comment> page = it.next();  // rest call
				
				
				List<Comment> filtered = page.stream()
						.filter(comment -> comment.getCreated().after(lastDate)).collect(Collectors.toList());
						
				
				
				
				if(filtered.size() > 0) {
					totals.addAll(filtered);
				} else {
					break;
				}
				
				
			}
			
			
			if(!totals.isEmpty()) {
				
				totals.forEach(comment -> {
					receiver.store(new SocialData(comment));
				});
				
				Comment recent = totals.getFirst();
				
				lastDate.setTime(recent.getCreated().getTime());
			}
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
	
		}
	}
}
