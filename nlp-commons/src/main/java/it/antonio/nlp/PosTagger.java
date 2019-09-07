package it.antonio.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

public class PosTagger {
	
	POSTaggerME possTaggerME;
	String[] posTags;
	String[] posTagsSimple;
	
	
	public PosTagger(POSTaggerME possTaggerME) {
		super();
		this.possTaggerME = possTaggerME;
		this.posTags = this.possTaggerME.getAllPosTags();
		
		this.posTagsSimple = Stream.of(this.posTags).map(s -> s.substring(0, 1)).distinct().toArray(i-> new String[i]);
	}

	public static PosTagger create() {

		try {
			
			InputStream stream = PosTagger.class.getClassLoader().getResourceAsStream("aciapatti_pos_training_model.dat");
			POSModel model = new POSModel(stream);
			
			POSTaggerME possTaggerME = new POSTaggerME(model);
			

			return new PosTagger(possTaggerME);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading aciapatti_pos_training_model file", e);
		}
	}

	public String[] tag(String[] tokens) {
		String[] tags = possTaggerME.tag(tokens);
		
		return tags;
	}
	
	public String[] tagSimple(String[] tokens) {
		String[] tags = possTaggerME.tag(tokens);
		
		return Stream.of(tags).map(s -> s.substring(0, 1)).toArray(i-> new String[i]);
	}

	public String[] getPosTags() {
		return posTags;
	}

	public String[] getPosTagsSimple() {
		return posTagsSimple;
	}
	
	
	
}
