package it.antonio.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

public class NerFinder {
	
	NameFinderME nameFinder;
	
	public NerFinder(NameFinderME nameFinder) {
		super();
		this.nameFinder = nameFinder;
	}

	public static NerFinder create() {

		try {
			
			InputStream stream = MorphitLemmatizer.class.getClassLoader().getResourceAsStream("wiki_ann_ner_training_model.dat");
			TokenNameFinderModel model = new TokenNameFinderModel(stream);
			
			NameFinderME nameFinder = new NameFinderME(model);
			

			return new NerFinder(nameFinder);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading wiki_ann_ner_training_model file", e);
		}
	}

	public String[] convert(String[] tokens) {
		String[] ners = new String[tokens.length];
		Span[] spans = nameFinder.find(tokens);
		
		for(Span span: spans) {
			for(int i = span.getStart(); i < span.getEnd(); i++ ) {
				ners[i] = span.getType();
			}
		}
		nameFinder.clearAdaptiveData();
		return ners;
	}
	
	
}
