package com.poeticjustice.deeppoemsinc.infrastructure.config.rabbit;

import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(RabbitMQConfig.RabbitQueuesProperties.class)
public class RabbitMQConfig {

    public static final String FILE_UPLOAD_QUEUE = "file.upload.queue";
    public static final String FILE_PROCESS_QUEUE = "file.process.queue";

    @ConfigurationProperties(prefix = "app.rabbit")
    public static class RabbitQueuesProperties {
        private List<String> queues;

        public List<String> getQueues() {
            return queues;
        }

        public void setQueues(List<String> queues) {
            this.queues = queues;
        }
    }

    @Bean
    public Declarables declareQueues(RabbitQueuesProperties props) {
        List<Queue> queues = props.getQueues().stream()
                .map(name -> new Queue(name, true, false, false)) // Durable, non-exclusive, non-auto-delete
                .collect(Collectors.toList());
        return new Declarables(queues);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            RetryOperationsInterceptor retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(retryInterceptor);
        factory.setMissingQueuesFatal(false); // Prevent crashes if queues are not yet declared
        factory.setConcurrentConsumers(2); // Allow parallel processing
        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000) // Initial 1s, multiplier 2, max 10s
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }
}