package it.antonio.util;

import java.util.function.Function;

import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;

public class WrapperSentenceIterator implements SentenceIterator{
	SentenceIterator delegate;
	Function<String, String> onSequence;
	
	public WrapperSentenceIterator(SentenceIterator delegate, Function<String, String> onSequence) {
		super();
		this.delegate = delegate;
		this.onSequence = onSequence;
	}

	public String nextSentence() {
		String sentence =  delegate.nextSentence();
		return onSequence.apply(sentence);
	}

	public boolean hasNext() {
		return delegate.hasNext();
	}

	public void reset() {
		delegate.reset();
	}

	public void finish() {
		delegate.finish();
	}

	public SentencePreProcessor getPreProcessor() {
		return delegate.getPreProcessor();
	}

	public void setPreProcessor(SentencePreProcessor preProcessor) {
		delegate.setPreProcessor(preProcessor);
	}
	
	
}
