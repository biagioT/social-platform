package it.antonio.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

public class NerFinder implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private CopyOnWriteArrayList<NameFinderME> nameFinders = new CopyOnWriteArrayList<>();
	private TokenNameFinderModel model;
	
	
	public NerFinder(TokenNameFinderModel model) {
		super();
		this.model = model;
		NameFinderME nameFinder = new NameFinderME(model);
		
		this.nameFinders.add(nameFinder);
	}

	public static NerFinder create() {

		try {
			
			InputStream stream = MorphitLemmatizer.class.getClassLoader().getResourceAsStream("wiki_ann_ner_training_model.dat");
			TokenNameFinderModel model = new TokenNameFinderModel(stream);
			
			
			return new NerFinder(model);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading wiki_ann_ner_training_model file", e);
		}
	}

	public String[] convert(String[] tokens) {
		
		NameFinderME nameFinder = null;
		while(nameFinder == null) {
			nameFinder = !nameFinders.isEmpty() ? nameFinders.remove(0) : null;
			if(nameFinder == null) {
				nameFinders.add(new NameFinderME(model));
			}
		}
				
		
		String[] ners = new String[tokens.length];
		Span[] spans = nameFinder.find(tokens);
		
		for(Span span: spans) {
			for(int i = span.getStart(); i < span.getEnd(); i++ ) {
				ners[i] = span.getType();
			}
		}
		nameFinder.clearAdaptiveData();
		nameFinders.add(nameFinder);
		return ners;
	}
	
	
}
