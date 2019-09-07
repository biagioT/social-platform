package it.antonio.datapush;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class DataSender {

	private MessageProducer producer;
	private Session session;
	
	public DataSender(MessageProducer producer, Session session) {
		super();
		this.producer = producer;
		this.session = session;
	}

	public void sendData(Object data) {
		
		try {
			String json = JsonUtils.toJson(data);
			TextMessage message = session.createTextMessage(json);
			producer.send(message);
			
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
		
		
	}
	


	public static DataSender create(String url, String username, String password) throws Exception {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL(url);
		//connectionFactory.setPassword(username);
		//connectionFactory.setUserName(password);
		final Connection connection = connectionFactory.createConnection();
		//connection.setClientID(username);
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Destination dataPushTopic = session.createTopic("data-push");
		MessageProducer dataPushProducer = session.createProducer(dataPushTopic);

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
		
		return new DataSender(dataPushProducer, session);
	}
}
