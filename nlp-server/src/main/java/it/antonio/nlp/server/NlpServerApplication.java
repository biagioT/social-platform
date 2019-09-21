package it.antonio.nlp.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class NlpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NlpServerApplication.class, args);
    }
    
    @RefreshScope
    @RestController
    class MessageRestController {

        @Value("${app.version}")
        private String version;

        @RequestMapping("/info")
        String getMessage() {
            return "nlp server v" + version ;
        }
    }
}