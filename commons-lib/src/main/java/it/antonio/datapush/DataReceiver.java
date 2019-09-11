package it.antonio.datapush;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import it.antonio.api.JsonUtils;

public class DataReceiver {
	
	MessageConsumer consumer;
	Session session;
	
	public DataReceiver(MessageConsumer consumer, Session session) {
		super();
		this.consumer = consumer;
		this.session = session;
	}
	
	
	public static DataReceiver create(String url, String username, String password, final DataPushListener l) throws Exception {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL(url);
		//connectionFactory.setPassword(username);
		//connectionFactory.setUserName(password);
		final Connection connection = connectionFactory.createConnection();
		//connection.setClientID(username);
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Destination dataPushTopic = session.createTopic("data-push");
		MessageConsumer dataPushConsumer = session.createConsumer(dataPushTopic);

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				try {
					connection.close();

				} catch (JMSException e) {
					throw new RuntimeException(e);
				}
			}
		});
		dataPushConsumer.setMessageListener(new MessageListener() {
			
			public void onMessage(Message message) {
				BufferedReader br = null;
				
				try {
					TextMessage om = (TextMessage) message;
					br = new BufferedReader(new StringReader(om.getText()));
					String dataType = br.readLine();
					
					String line, json="";
					while ((line = br.readLine()) != null) {
						json+=line;
					}
					br.close();
					
					Object data = JsonUtils.fromJson(json, DataTypes.valueOf(dataType).objectType());
					
					l.onData(data);
					
				} catch (Exception e) {
					if(br != null)
						try {
							br.close();
						} catch (IOException e1) {
							throw new RuntimeException(e);
						}
					throw new RuntimeException(e);
				}
				
			}
		});
		return new DataReceiver(dataPushConsumer, session);
	}
	
	
	public static interface DataPushListener {
		void onData(Object data);
	}
}
