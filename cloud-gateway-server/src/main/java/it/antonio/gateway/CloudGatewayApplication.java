package it.antonio.gateway;

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
@EnableDiscoveryClient
@SpringBootApplication
public class CloudGatewayApplication {

	private static final String URI_NLP_SERVER = "lb://nlp-server";
	private static final String URI_DATA_PUSH = "lb://data-push";
	
	@Bean
	public RouteLocator customRouteLocator(final RouteLocatorBuilder builder) {
		return builder.routes() //
				.route("nlp-server", r -> r.path("/nlp-server/**").filters(f -> f.preserveHostHeader().rewritePath("/nlp-server/(?<segment>.*)", "/${segment}")).uri(URI_NLP_SERVER)) //
				.route("data-push", r -> r.path("/data-push/**").filters(f -> f.rewritePath("/data-push/(?<segment>.*)", "/${segment}")).uri(URI_DATA_PUSH)) //
				.build();
	}

	
	public static void main(final String[] args) {
		SpringApplication.run(CloudGatewayApplication.class, args);
	}

}