package com.poeticjustice.deeppoemsinc.application.events;

import com.poeticjustice.deeppoemsinc.domain.Repository.mongodb.*;
import com.poeticjustice.deeppoemsinc.domain.models.mongo.AcceptedIps;
import com.poeticjustice.deeppoemsinc.domain.models.mongo.PendingIp;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class IpEventListener {

    private final AcceptedIpsRepository acceptedIpsRepository;
    private final PendingIpRepository pendingIpRepository;
    private AcceptedIps acceptedIps;

    public IpEventListener(AcceptedIpsRepository acceptedIpsRepository, PendingIpRepository pendingIpRepository) {
        this.acceptedIpsRepository = acceptedIpsRepository;
        this.pendingIpRepository = pendingIpRepository;
    }

    @EventListener
    public void handleNewIp(NewIPDetectedEvent event) {
        // Save a pending IP entry so admin can approve later.
        PendingIp p = new PendingIp();
        p.setUserId(event.getUserId());
        p.setIpAddress(event.getIp());
        p.setDetectedAt(LocalDateTime.now());
        p.setStatus("PENDING");
        pendingIpRepository.save(p);

        // You can also trigger email/sms here or publish to RabbitMQ for async processing.
        System.out.println("New IP detected and stored as pending: " + event.getIp());
    }
}