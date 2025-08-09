package com.poeticjustice.deeppoemsinc.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poeticjustice.deeppoemsinc.Repository.mongodb.DeadLetterRepository;

@RestController
@RequestMapping("/health")
public class HealthController {
    @Autowired
    private DeadLetterRepository deadLetterRepository;

    @GetMapping()
    public ResponseEntity<String> getHealth() {
        // This endpoint can be used to check the health of mongodb connection
        try {
            deadLetterRepository.findAll();
            return ResponseEntity.ok("Service is healthy");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Service is unhealthy: " + e.getMessage());
        }
    }    
}