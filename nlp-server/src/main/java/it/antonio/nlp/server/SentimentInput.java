package it.antonio.nlp.server;

import java.util.List;

public class SentimentInput {
	private String text;
	private List<String> textList;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public List<String> getTextList() {
		return textList;
	}
	public void setTextList(List<String> textList) {
		this.textList = textList;
	}
	
	
}
