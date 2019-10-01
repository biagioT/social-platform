package it.antonio.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * Cloud Gateway
 * 
 * @author biagiot
 *
 */
@SpringBootApplication
public class CloudGatewayApplication {

	@Value("${routes.nlp-server}")
	private String nlpServer;
	@Value("${routes.data-pusher}")
	private String dataPusher;
	@Value("${routes.site}")
	private String site;
	
	
	@Bean
	public RouteLocator customRouteLocator(final RouteLocatorBuilder builder) {
		return builder.routes() //
				.route("nlp-server", r -> r.path("/nlp-server/**").filters(f -> f.preserveHostHeader().rewritePath("/nlp-server/(?<segment>.*)", "/${segment}")).uri(nlpServer)) //
				.route("data-pusher", r -> r.path("/data-pusher/**").filters(f -> f.rewritePath("/data-pusher/(?<segment>.*)", "/${segment}")).uri(dataPusher)) //
				.route("site", r -> r.path("/site/**").filters(f -> f.rewritePath("/site/(?<segment>.*)", "/${segment}")).uri(site)) //
				.build();
	}

	
	public static void main(final String[] args) {
		SpringApplication.run(CloudGatewayApplication.class, args);
	}

}