package it.antonio.nlp.server;

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

	@PostMapping("/sentiment")
	public SentimentRNNResult survey(@RequestBody String text) {
		SentimentRNNResult output = sentimentRNN.sentiment(text);
		return output;
	}

}
