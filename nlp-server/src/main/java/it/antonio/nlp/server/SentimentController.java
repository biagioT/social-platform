package it.antonio.nlp.server;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.antonio.nlp.commons.SentimentRNNResult;
import it.antonio.sentiment.SentimentRNN;

@RestController
public class SentimentController {

	@Autowired
	SentimentRNN sentimentRNN;

	@PostMapping(value="/sentiment", consumes = "text/plain")
	public SentimentRNNResult sentntiment(@RequestBody String text) {
		SentimentRNNResult output = sentimentRNN.sentiment(text);
		return output;
	}

	@PostMapping(value="/sentiment", consumes = "application/json")
	public Object sentntimentData(@RequestBody SentimentInput sentimentInput) {
		if(sentimentInput.getText() != null) {
			return sentimentRNN.sentiment(sentimentInput.getText());
		}
		if(sentimentInput.getTextList() != null) {
			return sentimentInput.getTextList().stream().map(sentimentRNN::sentiment).collect(Collectors.toList());
		}
		
		throw new IllegalArgumentException("Invalid input");
	}
	
	

}
