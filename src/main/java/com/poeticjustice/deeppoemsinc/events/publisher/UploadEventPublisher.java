package com.poeticjustice.deeppoemsinc.events.publisher;

import com.poeticjustice.deeppoemsinc.config.RabbitMQConfig;
import com.poeticjustice.deeppoemsinc.events.dto.UploadEventPayload;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class UploadEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public UploadEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUploadEvent(String userId, String fileName, String category) {
        UploadEventPayload payload = new UploadEventPayload(userId, fileName, category);

        // If no exchange, publish directly to queue
        rabbitTemplate.convertAndSend(RabbitMQConfig.FILE_UPLOAD_QUEUE, payload);

        // If using exchange:
        // rabbitTemplate.convertAndSend(RabbitMQConfig.FILE_UPLOAD_EXCHANGE,
        //                               RabbitMQConfig.FILE_UPLOAD_ROUTING_KEY,
        //                               payload);
    }
}
