package com.poeticjustice.deeppoemsinc.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poeticjustice.deeppoemsinc.Repository.mongodb.DeadLetterRepository;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.SMSResponseLogsRepository;

@RestController
@RequestMapping("/health")
public class HealthController {
    @Autowired
    private DeadLetterRepository deadLetterRepository;

    @Autowired
    private SMSResponseLogsRepository smsResponseLogsRepository;

    @GetMapping("/deadletter/count")
    public long getDeadLetterCount() {
        return deadLetterRepository.count();
    }

    @GetMapping("/health")
    public String getHealth() {
        return "OK";
    }
    
    @GetMapping("/smsresponse/count")
    public long getSmsResponseLogsCount() {
        return smsResponseLogsRepository.count();
    }
    
}