import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import twitter4j.Status;

public class TestTwitterReader {

	//@Test
	public void read() throws IOException, ClassNotFoundException {
		
		File tweetDir = new File("tweets");
		
		for(File file: tweetDir.listFiles()) {
			ObjectInputStream reader = new ObjectInputStream(new FileInputStream(file));
				
			@SuppressWarnings("unchecked")
			List<Status> tweets = (List<Status>) reader.readObject();
			
			tweets.stream().forEach(t -> {
				System.out.println(t.getUser().getLocation());
			});
			
			reader.close();
		}
		
		
		
		
		
	}
}
