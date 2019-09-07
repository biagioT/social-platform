package it.antonio.intent;

import java.util.List;
import java.util.Set;

public interface IntentIterator {
	boolean hasNext();
	String nextSentence();
	List<String> currentLabels();
	public void reset();
	
	Set<String> allIntents();
	
}
