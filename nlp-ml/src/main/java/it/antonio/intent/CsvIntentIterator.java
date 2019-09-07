package it.antonio.intent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVRecord;

public class CsvIntentIterator implements IntentIterator{
	private Set<String> intentList = new HashSet<String>();
	int index = 0;
	
	private List<CSVRecord> records;
	
	public CsvIntentIterator(List<CSVRecord> records) {
		super();
		this.records = records;
	}

	@Override
	public String nextSentence() {
		String out =  records.get(index).get(0);
		index++;
		
		return out;
		
	}

	@Override
	public boolean hasNext() {
		return index < records.size();
	}

	@Override
	public void reset() {
		index = 0;	
	}

	

	
	@Override
	public List<String> currentLabels() {
		List<String> labels = new ArrayList<String>();
		for(int i = 1; i < 5 && i < records.get(index - 1).size(); i++) {
			
			String label = records.get(index - 1).get(i);
			if(!intentList.contains(label)) {
				intentList.add(label);
			}
			
			labels.add(label);
			
		}
		if(labels.isEmpty()) {
			throw new IllegalArgumentException("No intent found at line: "+ (index - 1));
		}
		
		return labels;
	}

	@Override
	public Set<String> allIntents() {
		return intentList;
	}
	
}
