package com.poeticjustice.deeppoemsinc.events.consumer;


import com.poeticjustice.deeppoemsinc.events.dto.UploadEventPayload;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.poeticjustice.deeppoemsinc.config.RabbitMQConfig;

/**
 * Consumer for handling file upload events.
 * Listens to the upload queue and processes incoming messages.
 */

@Component
public class UploadEventConsumer {

    private final RabbitTemplate rabbitTemplate;

    public UploadEventConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.FILE_UPLOAD_QUEUE)
    public void handleUploadEvent(UploadEventPayload payload) {
        // Log or process event (e.g., audit logging)
        System.out.println("Received upload event: " + payload.getUserId() + ", " + payload.getFileName());
    }
}