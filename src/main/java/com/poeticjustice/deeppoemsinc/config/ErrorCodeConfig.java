package com.poeticjustice.deeppoemsinc.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ErrorCodeConfig {

    private static final Logger logger = LoggerFactory.getLogger(ErrorCodeConfig.class);

    @Bean
    public Map<String, Map<String, String>> errorCodes() {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("ErrorCode.json");
        try {
            if (!resource.exists()) {
                logger.error("ErrorCode.json not found in classpath. Returning empty map as fallback.");
                return new HashMap<>();
            }
            logger.info("Loading ErrorCode.json from classpath");
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (IOException e) {
            logger.error("Failed to load ErrorCode.json: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
}