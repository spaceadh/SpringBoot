package com.poeticjustice.deeppoemsinc.application.events.consumer;

import com.poeticjustice.deeppoemsinc.application.events.UploadEventPayload;
import com.poeticjustice.deeppoemsinc.infrastructure.config.RabbitMQConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class UploadEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UploadEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.FILE_UPLOAD_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleUploadEvent(UploadEventPayload payload) {
        logger.info("Received upload event: {}", payload);
        // Process event
    }
}
