package com.poeticjustice.deeppoemsinc.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
// import org.springframework.amqp.core.Exchange;
// import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue name constant
    public static final String FILE_UPLOAD_QUEUE = "file.upload.queue";

    // Routing key (if you ever switch to Exchange-based routing)
    // public static final String FILE_UPLOAD_ROUTING_KEY = "file.upload.key";

    // Exchange name (commented since you said no exchange for now)
    // public static final String FILE_UPLOAD_EXCHANGE = "file.upload.exchange";

    /**
     * Define the queue for file upload events.
     */
    @Bean
    public Queue fileUploadQueue() {
        return new Queue(FILE_UPLOAD_QUEUE, true); // durable queue
    }

    // If you decide to use an exchange later, uncomment this
    /*
    @Bean
    public Exchange fileUploadExchange() {
        return new TopicExchange(FILE_UPLOAD_EXCHANGE, true, false);
    }

    @Bean
    public Binding binding(Queue fileUploadQueue, Exchange fileUploadExchange) {
        return BindingBuilder.bind(fileUploadQueue)
                .to(fileUploadExchange)
                .with(FILE_UPLOAD_ROUTING_KEY)
                .noargs();
    }
    */
}
