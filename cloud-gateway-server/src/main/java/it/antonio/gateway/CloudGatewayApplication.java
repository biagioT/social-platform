package it.antonio.gateway;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * Cloud Gateway
 * 
 * @author biagiot
 *
 */
@SpringBootApplication
public class CloudGatewayApplication {

	 Logger log = LoggerFactory.getLogger(CloudGatewayApplication.class);

	@Value("${routes.nlp-server}")
	private String nlpServer;
	@Value("${routes.data-pusher}")
	private String dataPusher;
	@Value("${routes.web-site}")
	private String webSite;
	
	
	@Bean
	public RouteLocator customRouteLocator(final RouteLocatorBuilder builder) {
		return builder.routes() //
				.route("nlp-server", r -> r.path("/nlp-server/**").filters(f -> f.preserveHostHeader().rewritePath("/nlp-server/(?<segment>.*)", "/${segment}")).uri(nlpServer)) //
				.route("data-pusher", r -> r.path("/data-pusher/**").filters(f -> f.rewritePath("/data-pusher/(?<segment>.*)", "/${segment}")).uri(dataPusher)) //
				.route("web-site", r -> r.path("/web-site/**").filters(f -> f.preserveHostHeader().rewritePath("/web-site/(?<segment>.*)", "/${segment}")).uri(webSite)) //
				.build();
	}

	
	public static void main(final String[] args) {
		SpringApplication.run(CloudGatewayApplication.class, args);
	}

	
	@Bean
	@Order(-1)
	public GlobalFilter a() {
	    return (exchange, chain) -> {
	    	Set<URI> uris = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, Collections.emptySet());
	        String originalUri = (uris.isEmpty()) ? "Unknown" : uris.iterator().next().toString();
	        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
	        URI routeUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
	        log.info("Incoming request " + originalUri + " is routed to id: " + route.getId()  + ", uri: " + routeUri);
	        return chain.filter(exchange);
	    };
	}

	
	
}