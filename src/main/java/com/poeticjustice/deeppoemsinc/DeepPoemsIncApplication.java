package com.poeticjustice.deeppoemsinc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class DeepPoemsIncApplication {

    public static void main(String[] args) {
        // Load Dotenv
        Dotenv dotenv = Dotenv.configure().load();
        // Set environment variables as system properties
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        
        SpringApplication.run(DeepPoemsIncApplication.class, args);
    }
}