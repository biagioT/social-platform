package it.antonio.nlp.train;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import opennlp.tools.namefind.BioCodec;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.WordTagSampleStream;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

public class PosTrainer {
	public static void main(String...args) throws Exception {
		
		ObjectStream<String> lineStream = new PlainTextByLineStream(new InputStreamFactory() {

			@Override
			public InputStream createInputStream() throws IOException {
				return new FileInputStream("/run/media/antonio/disco2/nlp/pos-aciapetti/it-train-perceptron.pos");
			}
			
		}, StandardCharsets.UTF_8);
		ObjectStream<POSSample> sampleStream = new WordTagSampleStream(lineStream);
		
		
		
		
		System.setProperty("java.io.tmpdir", "/run/media/antonio/disco2/nlp/tmp-training-ner/");
		
		TrainingParameters params = TrainingParameters.defaultParams();
		params.put(TrainingParameters.ITERATIONS_PARAM, 50);
		params.put(TrainingParameters.CUTOFF_PARAM, 3);
		   
		POSModel model = POSTaggerME.train("it", sampleStream, params, new POSTaggerFactory());
		
		

		BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream(new File("src/main/resources/aciapatti_pos_training_model.dat" ) ));
		model.serialize(modelOut);
		
	}
}
