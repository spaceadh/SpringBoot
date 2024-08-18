package com.poeticjustice.deeppoemsinc.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class GreetingController {

    @GetMapping
    public String sayHello() {
        String text = "Hello world"; // Added missing semicolon
        return text;
    }
}
