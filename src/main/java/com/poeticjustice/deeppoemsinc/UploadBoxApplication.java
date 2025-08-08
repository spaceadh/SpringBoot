package com.poeticjustice.deeppoemsinc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@EnableScheduling
@EnableRetry
public class UploadBoxApplication {

    public static void main(String[] args) {
        // Load Dotenv
        Dotenv dotenv = Dotenv.configure().load();
        // Set environment variables as system properties
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        
        SpringApplication.run(UploadBoxApplication.class, args);
    }
}