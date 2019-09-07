package it.antonio.intent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.junit.jupiter.api.Test;

import it.antonio.intent.pv.ParagraphVectorsIntentClassifier;
import it.antonio.nlp.NLPPipeline;

class ParagraphVectorsIntentClassifierTest {

	@Test
	void test() throws IOException {
		
		NLPPipeline pipeline = NLPPipeline.create();
		ParagraphVectorsIntentClassifier clf = new ParagraphVectorsIntentClassifier(new File("test-folder"), pipeline);
		
		
		
		final Reader reader = new InputStreamReader(new BOMInputStream(new FileInputStream(new File("test-folder", "intent.csv"))), "UTF-8");
		final CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
		
		List<CSVRecord> records = parser.getRecords();
	
		clf.train();
		for(CSVRecord r: records) {
			String text =r.get(0); 
			Object[] found = new Object[] {null, -1d};
			
			clf.findIntent(text).forEach((intentKey, score) -> {
				if(score > (double) found[1]) {
					found[0] = intentKey;
					found[1] = score;
					
				}
			}); 
			System.out.println(text + " " + found[0] + " " + r.get(1));
		}
		
		parser.close();
	}

}
