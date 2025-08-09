package com.poeticjustice.deeppoemsinc.application.events.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.poeticjustice.deeppoemsinc.application.dtos.UploadEventPayload;

@Service
public class UploadEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(UploadEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public UploadEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUploadEvent(UploadEventPayload payload) {
        try {
            rabbitTemplate.convertAndSend("file.upload.queue", payload);
            logger.info("Published upload event: {}", payload);
        } catch (Exception ex) {
            logger.error("Failed to publish upload event. Retrying...", ex);
            // Optionally add retry logic here or Spring Retry wrapper
        }
    }
}
