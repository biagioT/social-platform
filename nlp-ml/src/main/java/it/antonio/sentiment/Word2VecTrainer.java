package it.antonio.sentiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BaseSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;

import it.antonio.nlp.NLPPipeline;
import it.antonio.nlp.SentenceDetector;
import it.antonio.nlp.dl4j.NLPTokenizerFactory;
import it.antonio.nlp.train.PaisaSentenceIterator;
import it.antonio.tweet.SentipolcSentenceIterator;
import it.antonio.tweet.TweetData;
import it.antonio.util.WrapperSentenceIterator;

public class Word2VecTrainer implements Constants {
	
	public static void main(String...args) throws Exception {
		
		// train on paisa and tweets
		// all words from sentiment dataset are in the word2vec model
		
		File input = new File("/run/media/antonio/disco2/nlp/paisa/paisa.raw.utf8");
		
		List<String> sentences = new LinkedList<>();
		PaisaSentenceIterator it = new PaisaSentenceIterator(new BufferedReader(new FileReader(input)));
				
		SentenceDetector sd = SentenceDetector.create();
				
		while(it.hasNext()) {
			String line = it.next();
			Stream.of(sd.tag(line)).forEach(sentences::add);
		}
		
		System.out.println("Sentences in paisa: " + sentences.size());
		
		File csv1 = new File(Word2VecTrainer.class.getClassLoader().getResource("training_set_sentipolc16.csv").toURI());
		SentipolcSentenceIterator sit = new SentipolcSentenceIterator(new BufferedReader(new FileReader(csv1)));

		sit.forEachRemaining(t -> { sentences.add(t.text); });
		
		File csv2 = new File(Word2VecTrainer.class.getClassLoader().getResource("test_set_sentipolc16_gold2000.csv").toURI());
		sit = new SentipolcSentenceIterator(new BufferedReader(new FileReader(csv2)));
		sit.forEachRemaining(t -> { sentences.add(t.text); });
		
		System.out.println("Sentences in paisa+tweets: " + sentences.size());
		
		train(sentences, sentences.size(), new File(FILE_WORD2VEC));
	}
	
	
	private static Word2Vec train(Iterable<String> iterable, int size, File outputModelFile) {
		NLPTokenizerFactory tokenizerFactory = new NLPTokenizerFactory(NLPPipeline.create());

		SentenceIterator iterator = new BaseSentenceIterator() {
			Iterator<String> it = iterable.iterator();
			@Override
			public void reset() {
				it = iterable.iterator();
			}
			
			@Override
			public String nextSentence() {
				return it.next();
			}
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
		};
        
        
        
		double nLines = size;
        AtomicLong count = new AtomicLong();
        
        iterator = new WrapperSentenceIterator(iterator, sentence->{
        	
        	if(count.incrementAndGet() % 1000 == 0) {
        		double current = count.get();
        		double status = current / (nLines * 2) * 100;
        		status = (double)Math.round(status * 100) / 100d;
        		
        		
        		System.out.println("Status :" +  status + "%");
        	}
        	return sentence;
        });
        
        
         
        // epoch generally is 1
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(1)
                .iterations(1)
                .layerSize(WORD_SIZE)
                .seed(42)
                .windowSize(5)
                .iterate(iterator)
                .tokenizerFactory(tokenizerFactory)
                .build();

		vec.fit();
		
		
		WordVectorSerializer.writeWord2VecModel(vec, outputModelFile);
		count.set(0);
		
		return vec;
	}
	
}
