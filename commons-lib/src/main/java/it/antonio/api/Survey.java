package it.antonio.api;

import java.util.HashMap;
import java.util.Map;

public class Survey {
	private String name;
	private Map<String, Object> data = new HashMap<String, Object>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, Object> getData() {
		return data;
	}
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	
	
}
