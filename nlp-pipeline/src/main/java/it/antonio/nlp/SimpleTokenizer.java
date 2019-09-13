package it.antonio.nlp;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SimpleTokenizer implements Serializable {

	private static final long serialVersionUID = 1L;
	/*
	public static void main(String...args) {
		SimpleTokenizer tokenizer = SimpleTokenizer.create();
		String[] output = tokenizer.tokenize("che ci frega di mario #monti, noi sig.re abbiamo mario balotelli!");
		//String[] output = tokenizer.tokenize(":( il U.s.a Sig.re #ok ilpizze@hotmai.net http://www.google.com/ok/no?tes=tu Spiezia 24,5 12/34/1998 another@gmai.dot :) non-va-bene ");
		System.out.println(Arrays.toString(output));
	}*/
	
	
	private AbbreviatonsTrie abbreviations;
	private List<Character> sentenceSeparators;
	private Map<String, Pattern> regexList;
	
	private SimpleTokenizer(AbbreviatonsTrie abbreviations, List<Character> sentenceSeparators, Map<String, Pattern> regexList) {
		super();
		this.abbreviations = abbreviations;
		this.sentenceSeparators = sentenceSeparators;
		this.regexList = regexList;
	}
	
	public static SimpleTokenizer create() {
		Properties props = new Properties();
		try {
			props.load(SimpleTokenizer.class.getClassLoader().getResourceAsStream("tokenizer.properties"));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		
		AbbreviatonsTrie abbreviations = new AbbreviatonsTrie();
		List<Character> sentenceSeparators = new ArrayList<>();
		
		for(String abbr: props.getProperty("token.abbr").split(",")) {
			abbreviations.add(abbr.toLowerCase());
		}
		for(String ch: props.getProperty("sentence.separators").split(",")) {
			
			sentenceSeparators.add((char) Integer.parseInt(ch, 16));
		}
		
		
		Map<String, Pattern> regexList = props.keySet().stream()
				.filter(p -> ((String)p).startsWith("special.regex") )
				.map(p-> new Object[] { ((String) p).replace("special.regex.", ""), Pattern.compile(props.getProperty((String) p),Pattern.CASE_INSENSITIVE ) })
				.collect(Collectors.toMap(o -> (String) o[0], o -> (Pattern) o[1] ));
		
		return new SimpleTokenizer(abbreviations, sentenceSeparators, regexList);
	}

	public String[] computeSpecials(String[] tokens) {
		String[] specials = new String[tokens.length];
		for(int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			for(Entry<String, Pattern> regex: regexList.entrySet()) {
				Matcher matcher = regex.getValue().matcher(token);
				
				if(matcher.find()){
					specials[i] = regex.getKey();
				}
			}
		}
		return specials;
	}

	public String[] tokenize(String input) {
		
		Map<Integer, Integer> regexIndex = new TreeMap<>();
		for(Pattern regex: regexList.values()) {
			Matcher matcher = regex.matcher(input);
			
			while(matcher.find()){
				regexIndex.put(matcher.start(), matcher.end());
			}
		}
		
		
		
		ArrayList<String> strings = new ArrayList<>();
		List<Integer> sentenceIndex = new LinkedList<>();
		
		StringBuilder bld = new StringBuilder();
		
		char[] chars = input.toCharArray();
		for(int i=0; i < chars.length; i++) {
			i = computeRegex(i, regexIndex, input, strings);
			if(i >= chars.length) {
				break;
			}
			
			
			char c = chars[i];
			if(Character.isLetterOrDigit(c) || c == '\'') {
				bld.append(c);
				
				if(c == '\'') {
					String token = bld.toString();
					strings.add(token);
					bld.setLength(0);
					
					i = computeRegex(i, regexIndex, input, strings);
				}
			} else {
				
				String token = bld.toString();
				
				String abbreviation = computeAbbreviation(token, chars, i);
				if(abbreviation != null) {
					
					i += (abbreviation.length() - token.length());
					token = abbreviation;
					
				}
				
				if(bld.length() > 0) {
					strings.add(token);
					bld.setLength(0);
					i = computeRegex(i, regexIndex, input, strings);
				}
				
				
				if(abbreviation == null && sentenceSeparators.contains(c)) {
					sentenceIndex.add(strings.size());
				}
				
			}
		}
		
		if(bld.length() > 0) {
			strings.add(bld.toString());
		}
		
		sentenceIndex.add(strings.size());
		
		return strings.toArray(new String[]{});
	}
	
	private int computeRegex(int i, Map<Integer, Integer> regexIndex, String input, ArrayList<String> strings) {
		Integer end = regexIndex.get(i);
		
		if(end != null) {
			strings.add(input.substring(i, end));
			//return computeRegex(i + end, regexIndex, input, strings);
			return end;
		}
		
		return i;
	}

	private String computeAbbreviation(String token, char[] chars, int i) {
		String nextToken = token;
		String candidateAbbreviation = null;
		for(int j = 0; j < 7; j++) {
			List<String> abbrs = abbreviations.findStartsWith(nextToken.toLowerCase());
			
			
			
			// no abbreviation
			if(abbrs.size() == 0) {
				return candidateAbbreviation;
			}
			
			
			if(abbrs.size() == 1) {
				
				if(nextToken.toLowerCase().equalsIgnoreCase(abbrs.get(0).toLowerCase())) {
					return nextToken;
				} 
			}	
			
			// multiple -> its a valid candidate
			if(abbrs.size() > 1) {
				candidateAbbreviation = nextToken;
			}

			// end of chars
			if(i+j == chars.length) {
				return candidateAbbreviation;
			}
			
			// no match -> continue
			nextToken = nextToken += chars[i + j];
				
				
			
			
		}		

		return null;
	}

	private static class AbbreviatonsTrie implements Serializable{

		private static final long serialVersionUID = 1L;
		
		public TrieNode root;
		
		public AbbreviatonsTrie() {
			this.root = new TrieNode(' ');
		}

		public void add(String word) {
			
			if(word == null)  {
				return;
			}
			
			int length = word.length();

			TrieNode current = this.root;

			if (length == 0) {
				current.word = word;
			}
			for (int index = 0; index < length; index++) {

				char letter = word.charAt(index);
				TrieNode child = current.getChild(letter);

				if (child != null) {
					current = child;
				} else {
					current.children.put(letter, new TrieNode(letter));
					current = current.getChild(letter);
				}
				if (index == length - 1) {
					current.word = word;
				}
			}
		}

		
		public List<String> findStartsWith(String word) {
			TrieNode current = this.root;

			for (int index = 0; index < word.length(); index++) {
				char letter = word.charAt(index);
				
				TrieNode child = current.getChild(letter);

				if (child != null) {
					current = child;
				} else {
					return Collections.emptyList();
				}
				
			}
			
			List<String> values = new ArrayList<>();
			onLeaves(current, node -> {
				values.add(node.word);
			});
			return values;

		}

		
		
		
		private void onLeaves(TrieNode current, Consumer<TrieNode> c) {
			for(TrieNode child: current.children.values()) {
				onLeaves(child, c);
			}
			if(current.word != null) {
				c.accept(current);
			} 
			
		}

		
		private class TrieNode implements Serializable {

			private static final long serialVersionUID = 1L;

			public final int ALPHABET = 26;

			@SuppressWarnings("unused")
			public char letter;
			public String word;
			public Map<Character, TrieNode> children;

			public TrieNode(char letter) {
				this.letter = letter;
				children = new HashMap<Character, TrieNode>(ALPHABET);
			}

			public TrieNode getChild(char letter) {

				if (children != null) {
					if (children.containsKey(letter)) {
						return children.get(letter);
					}
				}
				return null;
			}
		}
	}


}