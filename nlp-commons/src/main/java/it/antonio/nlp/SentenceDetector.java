package it.antonio.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class SentenceDetector {
	
	SentenceDetectorME sentenceDetectorME;
	
	
	public SentenceDetector(SentenceDetectorME sentenceDetectorME) {
		super();
		this.sentenceDetectorME = sentenceDetectorME;
	}

	public static SentenceDetector create() {

		try {
			
			InputStream stream = SentenceDetector.class.getClassLoader().getResourceAsStream("aciapetti_sentence_model.dat");
			SentenceModel model = new SentenceModel(stream);
			
			SentenceDetectorME sentenceDetectorME = new SentenceDetectorME(model);
			

			return new SentenceDetector(sentenceDetectorME);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading aciapetti_sentence_model file", e);
		}
	}

	public String[] tag(String text) {
		return sentenceDetectorME.sentDetect(text);
	}
	
	
}
