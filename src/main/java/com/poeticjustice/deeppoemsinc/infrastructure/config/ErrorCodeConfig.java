package com.poeticjustice.deeppoemsinc.infrastructure.config;

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

    @Bean(name = "errorCodesMap")
    public Map<String, Map<String, String>> errorCodes() {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("ErrorCode.json");
        try {
            if (!resource.exists()) {
                logger.error("ErrorCode.json not found in classpath at path: {}", resource.getPath());
                return new HashMap<>();
            }
            if (!resource.isReadable()) {
                logger.error("ErrorCode.json is not readable at path: {}", resource.getPath());
                return new HashMap<>();
            }

            logger.info("Loading ErrorCode.json from classpath: {}", resource.getPath());
            Map<String, Map<String, String>> errorCodeMap = objectMapper.readValue(
                resource.getInputStream(), 
                new TypeReference<Map<String, Map<String, String>>>() {}
            );

            logger.info("Successfully loaded {} error codes", errorCodeMap.size());
            errorCodeMap.forEach((key, value) -> {
                logger.info("Loaded error code: {} with details: {}", key, value);
            });

            return errorCodeMap;
        } catch (IOException e) {
            logger.error("Failed to load ErrorCode.json: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
}