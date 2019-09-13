package it.antonio.nlp.server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.antonio.sentiment.SentimentRNN;

@Configuration
public class NlpConfig {

	@Value("${models.word2vec}")
	private File wor2vecModel;
	@Value("${models.rnn}")
	private File netModel;

	@Bean
	public SentimentRNN sentimentRNN() throws IOException, URISyntaxException {
		return SentimentRNN.create(wor2vecModel, netModel);
	}
	
}
