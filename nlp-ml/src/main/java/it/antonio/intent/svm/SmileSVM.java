package it.antonio.intent.svm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.sentenceiterator.BaseSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.nd4j.linalg.api.ndarray.INDArray;

import it.antonio.intent.CsvIntentIterator;
import it.antonio.intent.IntentIterator;
import it.antonio.intent.pv.MeansBuilder;
import it.antonio.nlp.NLPPipeline;
import it.antonio.sentiment.NLPTokenizerFactory;
import smile.classification.SVM;
import smile.math.kernel.GaussianKernel;

public class SmileSVM {

	public static void main(String... args) throws IOException {
		NLPPipeline pipeline = NLPPipeline.create();

		NLPTokenizerFactory tf = new NLPTokenizerFactory(pipeline);

		final Reader reader = new InputStreamReader(
				new BOMInputStream(new FileInputStream(new File("test-folder", "intent.csv"))), "UTF-8");
		final CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());

		List<CSVRecord> records = parser.getRecords();

		final IntentIterator it = new CsvIntentIterator(records);
		SentenceIterator iterator = new BaseSentenceIterator() {
			@Override
			public void reset() {
				it.reset();
			}

			@Override
			public String nextSentence() {
				String sentence = it.nextSentence();
				it.currentLabels(); // make more elegant
				return sentence;
			}

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
		};

		ParagraphVectors vec = new ParagraphVectors.Builder().minWordFrequency(1).iterations(1).layerSize(100).seed(42)
				.windowSize(5).iterate(iterator).tokenizerFactory(tf).build();

		vec.fit();

		List<String> intents = new ArrayList<>(it.allIntents());

		IntentIterator it2 = new CsvIntentIterator(records);
		List<double[]> train = new ArrayList<>();
		List<Integer> trainLabels = new ArrayList<>();

		MeansBuilder bld = new MeansBuilder(vec.getLookupTable(), tf);

		while (it2.hasNext()) {
			String sentence = it2.nextSentence();
			List<String> labels = it2.currentLabels();
			INDArray meaning = bld.documentAsVector(sentence);
			double[] vector = meaning.toDoubleVector();
			//for(int i = 0; i < vector.length; i++) vector[i] = 10000 * vector[i];
			
			int labelIndex = intents.indexOf(labels.get(0));
			trainLabels.add(labelIndex);

			train.add(vector);
		}

		double[][] x = train.toArray(new double[train.size()][]);
		int[] y = new int[trainLabels.size()];

		
		IntStream.range(0, trainLabels.size()).forEach(i -> {
			y[i] = trainLabels.get(i);
		});

		SVM<double[]> svm = new SVM<double[]>(new GaussianKernel(1E-4), 5.0, intents.size(), SVM.Multiclass.ONE_VS_ONE);

		svm.learn(x, y);
		it2.reset();

		while (it2.hasNext()) {
			String sentence = it2.nextSentence();
			List<String> labels = it2.currentLabels();
			INDArray meaning = bld.documentAsVector(sentence);
			double[] vector = meaning.toDoubleVector();
			//for(int i = 0; i < vector.length; i++) vector[i] = 10000 * vector[i];
			
			int out = svm.predict(vector);
			String predictedLabel = intents.get(out);

			System.out.println(labels.get(0) + " " + predictedLabel + " " + sentence);

		}

		svm.finish();

	}

}
