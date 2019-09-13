package it.antonio.nlp.train;

import java.io.BufferedReader;
import java.io.IOException;

import it.antonio.util.LazyIterator;

public class PaisaSentenceIterator extends LazyIterator<String>{
/*
	public static void main(String... args) throws URISyntaxException, IOException {
		File input = new File("/run/media/antonio/disco2/nlp/paisa/paisa.raw.utf8");
		
		//File output = new File("/home/antonio/priv/bigdata/SentimentTest/src/main/resources/paisa.raw.txt");
		//output.delete();
		//output.createNewFile();
		
		PaisaSentenceIterator it = new PaisaSentenceIterator(new BufferedReader(new FileReader(input)));
		
		SentenceDetector sd = SentenceDetector.create();
		while(it.hasNext()) {
			String line = it.next();
			
			System.out.println(line);
			System.out.println("@@@@@@@@");
			Stream.of(sd.tag(line)).forEach(System.out::println);
			System.out.println("@@@@@@@@");
			System.out.println("******************************************************");
			//Files.write(output.toPath(), sentences, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

		}
		//BufferedReader reader = new BufferedReader(new FileReader(output));
		//int lines = 0;
		//while (reader.readLine() != null) lines++;
		//reader.close();
		//System.out.println(lines);
				
		
	}
	
	*/
	
	
	BufferedReader reader;
	StringBuilder bld = new StringBuilder();
	String line;
	int num = 0;
	int numSentences = 0;
	int maxSentences = -1;
	
	public PaisaSentenceIterator(BufferedReader reader) {
		super();
		this.reader = reader;
		line = read();
	}
	public PaisaSentenceIterator(BufferedReader reader, int maxSentences) {
		this(reader);
		this.maxSentences = maxSentences;
	}

	@Override
	protected String computeNext() {
		if(maxSentences == numSentences) {
			return endOfData();
		}
		
		while(line != null) {
			if(line.length() == 0) {
				line = read(); continue;
			}
			if(line.charAt(0) == '#') {
				line = read(); continue;
			}
			if(line.startsWith("</te")) {
				line = read(); 
				numSentences++;
				return bld.toString();
			}
			if(line.startsWith("<te")) {
				bld.setLength(0);
				line = read(); continue;
			}
			
			
			bld.append(line);
			line = read();
		}
		
		
		return endOfData();
	}
	
	private String read() {
		try {
			num++;
			return reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}