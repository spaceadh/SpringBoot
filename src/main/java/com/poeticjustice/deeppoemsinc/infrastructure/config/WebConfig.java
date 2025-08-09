package com.poeticjustice.deeppoemsinc.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Example: Get allowed origins from application.properties, environment variable, DB, or hardcoded list

        // 1. From application.properties
        // String[] allowedOrigins = org.springframework.core.env.AbstractEnvironment.getProperty("app.cors.allowed-origins", String[].class);
        String[] allowedOrigins = null; // Replace with actual retrieval logic

        // 2. From environment variable (uncomment if needed)
        // @Value("${app.cors.allowed-origins:}")
        // private String corsAllowedOrigins;

        // if (corsAllowedOrigins != null && !corsAllowedOrigins.isEmpty()) {
        //     allowedOrigins = corsAllowedOrigins.split(",");
        // }

        // 3. From DB (pseudo-code, replace with actual DB call)
        if (allowedOrigins == null) {
            // allowedOrigins = dbService.getAllowedOrigins();
        }

        // 4. Fallback to hardcoded list
        if (allowedOrigins == null) {
            allowedOrigins = new String[] { "http://localhost:3000", "https://yourdomain.com" };
        }

        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true);
    }
}