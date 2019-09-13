package it.antonio.intent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;

import it.antonio.nlp.NLPPipeline;
import it.antonio.nlp.SentenceDetector;
import it.antonio.nlp.StopWords;
import it.antonio.nlp.dl4j.NLPTokenizerFactory;
import it.antonio.nlp.train.PaisaSentenceIterator;
import smile.clustering.KMeans;

public class TopicFinder {
	public static void main(String...args) throws FileNotFoundException {
		File input = new File("/run/media/antonio/disco2/nlp/paisa/paisa.raw.utf8");
		
		List<String> sentences = new LinkedList<>();
		PaisaSentenceIterator it = new PaisaSentenceIterator(new BufferedReader(new FileReader(input)), 200);
				
		SentenceDetector sd = SentenceDetector.create();
			
		
		while(it.hasNext()) {
			String line = it.next();
			Stream.of(sd.tag(line)).forEach(sentences::add);
		}
		
		System.out.println("Sentences in paisa: " + sentences.size());
		
		NLPPipeline nlpPipeline = NLPPipeline.create();
		NLPTokenizerFactory tf = new NLPTokenizerFactory(nlpPipeline );
		
		StopWords sw = StopWords.create();
		
		ParagraphVectors paragraphVectors = new ParagraphVectors.Builder()
                .minWordFrequency(1)
                .layerSize(100)
                .stopWords(new ArrayList<String>())
                .windowSize(3)
                .iterate(new CollectionSentenceIterator(sentences))
                .tokenizerFactory(tf)
                .batchSize(1000)
                .epochs(5)
                .build();
		
		paragraphVectors.fit();
		
		List<double[]> train = new ArrayList<>();
		
		for(String sentence: sentences) {
			double[] vector = paragraphVectors.inferVector(sentence).toDoubleVector();
			train.add(vector);
		}
		
		double[][] x = train.toArray(new double[train.size()][]);
		
		KMeans kMeans = new KMeans(x, 4);
		
		Map<Integer, List<String>> sentencesCategorized = new HashMap<>();
		
		for(String sentence: sentences) {
			double[] vector = paragraphVectors.inferVector(sentence).toDoubleVector();
			int label = kMeans.predict(vector);
			
			if(!sentencesCategorized.containsKey(label)) sentencesCategorized.put(label, new ArrayList<>());
			
			sentencesCategorized.get(label).add(sentence);
		}
		
		
		for(Entry<Integer, List<String>> e: sentencesCategorized.entrySet()) {
			Map<String, Integer> invertedIndex = new HashMap<String, Integer>(); 
			for(String sentence: e.getValue()) {
				List<String> tokens = nlpPipeline.tokens(sentence)
						.stream().map(t-> {
							return t.lemma != null ? t.lemma :  t.word; 
						}).collect(Collectors.toList());
				
				
				
				tokens.stream().filter(t -> !sw.isStopWord(t)).forEach(t ->  {
					if(!invertedIndex.containsKey(t)) invertedIndex.put(t, 0);
					
					int val = invertedIndex.get(t);
					invertedIndex.put(t, val + 1);
					
				});
			}
			
			
			
			
			System.out.println("LABEL " + e.getKey());
			
			new ArrayList<>(invertedIndex.entrySet()).stream()
			.sorted((a,b)-> b.getValue().compareTo(a.getValue()))
			.limit(25).forEach(entr-> {
				System.out.println(entr.getKey() + " " + entr.getValue());
			});
			
			
			System.out.println("*******************************************************");
			
		}
	}
}
