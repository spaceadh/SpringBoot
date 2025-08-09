package com.poeticjustice.deeppoemsinc.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class GreetingController {
    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Service is running");
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("FileUploader is up!");
    }
}