package it.antonio.datapush;

import java.util.ArrayList;
import java.util.List;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.antonio.api.Survey;
import it.antonio.datapush.DataReceiver;
import it.antonio.datapush.DataSender;

@Configuration
public class ActiveMQConfig {
	
	@Value("${activemq.url}")
	private String url;
	
	private String username;
	private String password;
	
	@Bean
	public BrokerService broker() throws Exception {
		
		List<AuthenticationUser> users = new ArrayList<>();
		users.add(new AuthenticationUser("activemq_user", "activemq_pw", "users"));

		

		SimpleAuthenticationPlugin authenticationPlugin = new SimpleAuthenticationPlugin(users);
		
		BrokerService broker = new BrokerService();
		broker.addConnector(url);
		//broker.setPlugins(new BrokerPlugin[] { authenticationPlugin, authorizationPlugin });
		broker.setPersistenceAdapter(new MemoryPersistenceAdapter());
		broker.setPersistent(false);
		
		broker.start();
		return broker;
	}
	
	@Bean
	public DataSender sender(BrokerService service) throws Exception {
		
		return DataSender.create(url, username, password);
	}
	
	@Bean
	public DataReceiver receiver(BrokerService service) throws Exception {
		
		return DataReceiver.create(url, username, password, new DataReceiver.DataPushListener() {
			
			@Override
			public void onData(Object data) {
				System.out.println("RICEVUTO MSG: " + ((Survey) data).getName() + " -- " + ((Survey) data).getData());
			}
		});
	}
}
