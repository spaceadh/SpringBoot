package com.poeticjustice.deeppoemsinc.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.minio.MinioClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    // Dotenv dotenv = Dotenv.load();
    // String minioUrl = dotenv.get("MINIO_URL", "http://localhost:9000");
    // String minioAccessKey = dotenv.get("MINIO_ACCESS_KEY", "minioadmin");
    // String minioSecretKey = dotenv.get("MINIO_SECRET_KEY", "minioadmin");
    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String minioAccessKey;

    @Value("${minio.secret-key}")
    private String minioSecretKey;
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
    }
}