package it.antonio.datapush;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

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
				try {
					TextMessage om = (TextMessage) message;
					TestData data = JsonUtils.fromJson(om.getText(), TestData.class);
					l.onData(data);
					
				} catch (JMSException e) {
					throw new RuntimeException(e);
				}
				
			}
		});
		return new DataReceiver(dataPushConsumer, session);
	}
	
	
	public static interface DataPushListener {
		void onData(TestData data);
	}
}
