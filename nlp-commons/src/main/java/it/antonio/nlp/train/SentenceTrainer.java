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
import opennlp.tools.sentdetect.SentenceDetectorFactory;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceSample;
import opennlp.tools.sentdetect.SentenceSampleStream;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

public class SentenceTrainer {
	public static void main(String...args) throws Exception {
		
		ObjectStream<String> lineStream =new PlainTextByLineStream(new InputStreamFactory() {
			
			@Override
			public InputStream createInputStream() throws IOException {
				return new FileInputStream("/run/media/antonio/disco2/nlp/sentence-aciapetti/it-train.sent");
			}
		}, StandardCharsets.UTF_8);

		ObjectStream<SentenceSample> sampleStream = new SentenceSampleStream(lineStream);
		
		System.setProperty("java.io.tmpdir", "/run/media/antonio/disco2/nlp/tmp-training-ner/");
		
		TrainingParameters params = TrainingParameters.defaultParams();
		params.put(TrainingParameters.ITERATIONS_PARAM, 50);
		params.put(TrainingParameters.CUTOFF_PARAM, 3);
		   
		SentenceModel model = SentenceDetectorME.train("it", sampleStream, SentenceDetectorFactory.create(null, "it", true, null, ".?!:;".toCharArray()), params);
		
		

		//File modelFile = new File("/home/antonio/priv/bigdata/sentiment-detection/src/main/resources/wiki_ann_ner_training_model2.dat");
		BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream(new File("src/main/resources/aciapetti_sentence_model.dat" ) ));
		model.serialize(modelOut);
		
	}
}
