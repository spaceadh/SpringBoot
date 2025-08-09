package com.poeticjustice.deeppoemsinc.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Map<String, String> gatewayConfiguration() {
        Map<String, String> config = new HashMap<>();
        // Load from application.properties or environment
        config.put("SMS:AfricasTalking:Chamasoft:Username", System.getenv("AT_USERNAME"));
        config.put("SMS:AfricasTalking:Chamasoft:ApiKey", System.getenv("AT_API_KEY"));
        config.put("SMS:AfricasTalking:Chamasoft:senderId", System.getenv("AT_SENDER_ID"));
        return config;
    }
}