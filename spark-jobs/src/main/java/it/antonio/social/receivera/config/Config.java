package it.antonio.social.receivera.config;

import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class Config {

	public static Properties load(String url, String username, String password) throws IOException {
		HttpClient client = new DefaultHttpClient();
		 
		HttpGet get = new HttpGet(url);
		String auth = username + ":" + password;
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
		String authHeader = "Basic " + new String(encodedAuth);
		get.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

		
		HttpContext context = new BasicHttpContext();
		
		
		HttpResponse response = client.execute(get, context );
		if(response.getStatusLine().getStatusCode() != 200) {
			throw new IllegalArgumentException("Cannot load configuration: " + url + " " + response.getStatusLine());
		}
		
		Properties properties = new Properties();
		properties.load(response.getEntity().getContent());
		return properties;
	}
	
	public static void main(String...args) throws IOException {
		System.out.println(load("http://localhost:6091/data-pusher.properties", "root", "toortoor"));
	}
}
