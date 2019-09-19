package it.antonio.gateway;

import java.net.URI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

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
				.route("service", r -> r.path("/data-push/**").filters(f -> f.rewritePath("/data-push/(?<segment>.*)", "/${segment}")).uri(URI_DATA_PUSH)) //
				.build();
	}

	
	public static void main(final String[] args) {
		SpringApplication.run(CloudGatewayApplication.class, args);
	}

}