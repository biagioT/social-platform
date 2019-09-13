package it.antonio.nlp.train;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class WikiNerConverter {

	public static void main(String...args) throws IOException {
		File input1 = new File("/run/media/antonio/disco2/nlp/Wikiner/ita/aij-wikiner-it-wp2");
		File input2 = new File("/run/media/antonio/disco2/nlp/Wikiner/ita/aij-wikiner-it-wp3");
		
		File output = new File("src/main/resources/nlp-training-models/wiki_ann_ner_training.txt");
		output.delete();
		output.createNewFile();
		
		BufferedReader reader1 =   new BufferedReader(new FileReader(input1));
		BufferedReader reader2 =   new BufferedReader(new FileReader(input2));
		BufferedWriter writer = new BufferedWriter(new FileWriter(output), 32768);
	    
		process(reader1, writer);
		process(reader2, writer);
		
		
	    
	    reader1.close();
	    reader2.close();
		writer.close();
	}
	
	private static void process(BufferedReader reader, BufferedWriter writer) throws IOException {
		String line;
		StringBuilder bld = new StringBuilder();
		
	    while ((line = reader.readLine()) != null) {
	    	String[] tokens = line.split(" ");
	    	for(String token: tokens) {
	    		String[] elements = token.split("\\|");
	    		if(elements.length == 3 && !elements[2].equals("O")) {
	    			bld.append("<START:" + elements[2]+"> "+ elements[0] + " <END> " );
	    		} else {
	    			bld.append( elements[0] +" ");
	    		}
	    	}
	    	writer.write(bld.toString());//appends the string to the file
	    	writer.write('\n');
	       
	    	bld.setLength(0);
	    }
	}

	
	
}
