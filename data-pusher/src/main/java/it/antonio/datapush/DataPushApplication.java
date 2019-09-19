package it.antonio.datapush;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
public class DataPushApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataPushApplication.class, args);
    }
    
    
    @RefreshScope
    @RestController
    class MessageRestController {

        @Value("${app.version}")
        private String version;

        @RequestMapping("/info")
        String getMessage() {
            return "data-push v" + version ;
        }
    }
}