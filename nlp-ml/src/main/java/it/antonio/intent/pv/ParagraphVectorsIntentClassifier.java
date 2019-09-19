package it.antonio.intent.pv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.sentenceiterator.labelaware.LabelAwareSentenceIterator;
import org.nd4j.linalg.api.ndarray.INDArray;

import it.antonio.intent.CsvIntentIterator;
import it.antonio.intent.IntentIterator;
import it.antonio.nlp.NLPPipeline;
import it.antonio.sentiment.NLPTokenizerFactory;

public class ParagraphVectorsIntentClassifier {
	
	private File folder;
	private ParagraphVectors paragraphVectors;
	private Set<String> intentList;
	private NLPTokenizerFactory tf;
	
	public ParagraphVectorsIntentClassifier(File folder, NLPPipeline nlpPipeline) {
		super();
		this.folder = folder;
		this.tf = new NLPTokenizerFactory(nlpPipeline);
	}
	
	public void train() throws IOException {
		
		final Reader reader = new InputStreamReader(new BOMInputStream(new FileInputStream(new File(folder, "intent.csv"))), "UTF-8");
		final CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
		
		List<CSVRecord> records = parser.getRecords();
		
		
		
		
		parser.close();
		reader.close();
		
		paragraphVectors = new ParagraphVectors.Builder()
                .minWordFrequency(1)
                .layerSize(100)
                .stopWords(new ArrayList<String>())
                .windowSize(3)
                //.iterate(it)
                .tokenizerFactory(tf)
                .batchSize(1000)
                //.epochs(epochs)
                .build();
		
		
		// first time  for labels
		IntentIterator intentIterator = new CsvIntentIterator(records);
		IntentSentenceIterator it = new IntentSentenceIterator(intentIterator); 
		paragraphVectors.setSentenceIterator(it);
		paragraphVectors.fit();
		intentList = intentIterator.allIntents();
		
		
		
		int testSetSize = (int) (records.size() * 0.2);
		int trainingSetSize = (int) (records.size() * 0.8);
		
		
		for (int epoch = 0; epoch < 100; epoch++) {

			Collections.shuffle(records);
			List<CSVRecord> trainRecords = records.subList(0, trainingSetSize);
			List<CSVRecord> testRecords = records.subList(trainingSetSize, testSetSize + trainingSetSize); 
			
			paragraphVectors.setSentenceIterator(new IntentSentenceIterator(new CsvIntentIterator(trainRecords)) );
			paragraphVectors.fit();
			
			double ok = 0;
			
			IntentSentenceIterator testIterator = new IntentSentenceIterator(new CsvIntentIterator(testRecords));
			while(testIterator.hasNext()) {
				String sentence = testIterator.nextSentence();
				List<String> labels = testIterator.currentLabels();
				Map<String, Double> intent = findIntent(sentence);
				
				Object[] found = new Object[] {null, -1d};
				
				intent.forEach((intentKey, score) -> {
					if(score > (double) found[1]) {
						found[0] = intentKey;
						found[1] = score;
						
					}
				}); 
				
				if(labels.contains(found[0])) {
					ok++;
				}
				
			};
			
			ok = ok / (double) testSetSize;
			
			if(ok > 0.8) {
				break;
			}
			
		}
		
		
		
		WordVectorSerializer.writeParagraphVectors(paragraphVectors, new File(folder, "intent-paragraph-vectors.dat"));
		
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(folder, "intent-list.dat")));
		
		os.writeObject(intentList);
		
		os.close();
	}
	
	@SuppressWarnings("unchecked")
	public void load() throws IOException, ClassNotFoundException {
		paragraphVectors = WordVectorSerializer.readParagraphVectors(new File(folder, "intent-paragraph-vectors.dat"));
		
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(new File(folder, "intent-list.dat")));
		
		intentList = (Set<String>) is.readObject();
		
		is.close();
		
		
	}
	
	
	public Map<String, Double> findIntent(String sententce){
		MeansBuilder meansBuilder = new MeansBuilder(paragraphVectors.getLookupTable(),  tf);
		LabelSeeker seeker = new LabelSeeker(intentList, paragraphVectors.getLookupTable());
		
		INDArray documentAsCentroid = meansBuilder.documentAsVector(sententce);
		
		return seeker.getScores(documentAsCentroid);
	}
	
	
	
	private static class IntentSentenceIterator implements LabelAwareSentenceIterator {

		private IntentIterator it;
		
		public IntentSentenceIterator(IntentIterator it) {
			super();
			this.it = it;
		}

		@Override
		public String nextSentence() {
			return it.nextSentence();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public void reset() {
			it.reset();
		}
		
		@Override
		public void finish() {
			
		}

		@Override
		public SentencePreProcessor getPreProcessor() {
			return null;
		}

		@Override
		public void setPreProcessor(SentencePreProcessor preProcessor) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String currentLabel() {
			return it.currentLabels().get(0);
		}

		@Override
		public List<String> currentLabels() {
			return it.currentLabels();
		}
		
	}
	
	
	
	
	
	
}
