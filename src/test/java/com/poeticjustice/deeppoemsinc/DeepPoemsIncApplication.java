package com.poeticjustice.deeppoemsinc;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class DeepPoemsIncApplication implements CommandLineRunner {

	public static final Logger logger = LoggerFactory.getLogger(DeepPoemsIncApplication.class);
	public static void main(String[] args) {
		logger.info("Starting application");
		SpringApplication.run(DeepPoemsIncApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("Application started");
	}
}