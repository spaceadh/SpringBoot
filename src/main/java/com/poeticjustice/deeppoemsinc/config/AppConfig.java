package com.poeticjustice.deeppoemsinc.config;

import com.poeticjustice.deeppoemsinc.helpers.ValidationHelper;
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
    public ValidationHelper validationHelper() {
        return new ValidationHelper();
    }
}