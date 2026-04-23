package org.acme.api;

import org.acme.api.config.SpringdocProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SpringdocProperties.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}