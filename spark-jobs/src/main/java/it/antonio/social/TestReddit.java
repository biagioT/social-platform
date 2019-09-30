package it.antonio.social;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.BarebonesPaginator;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.SubredditReference;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;

public class TestReddit {

	public static void main(String... args) throws InterruptedException {
		UserAgent userAgent = new UserAgent("analyzer", "it.antonio", "v0.1", "ilpizze");

		Credentials credentials = Credentials.script("ilpizze", "p12124545", "YBlWgMmCpNGGxQ", "IDxAHgTKRtdD9Ds2300JUzjRZ94");

		// This is what really sends HTTP requests
		NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);

		// Authenticate and get a RedditClient instance
		RedditClient reddit = OAuthHelper.automatic(adapter, credentials);
		
		SubredditReference subreddit = reddit.subreddit("italy");
		
		
		
		Date now = new Date();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		calendar.add(Calendar.MINUTE, -50);
		now = calendar.getTime();
		Date lastDate = now;
		
		while(true) {
			
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
				Comment recent = totals.getFirst();
				
				lastDate.setTime(recent.getCreated().getTime());
			}
			
			Thread.sleep(10000);
	
		}
		
	}

	
	
	
}
