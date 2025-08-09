package com.poeticjustice.deeppoemsinc.infrastructure.config.rabbit;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.*;
import jakarta.annotation.PostConstruct;

import java.*;
import java.util.*;

@Component
public class RabbitMQQueueManager implements ConnectionListener {

    private final RabbitAdmin rabbitAdmin;
    private final List<Queue> queuesToDeclare;

    @Autowired
    public RabbitMQQueueManager(ConnectionFactory connectionFactory, List<Queue> queuesToDeclare) {
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
        this.queuesToDeclare = queuesToDeclare;
        // Add this component as connection listener to handle reconnection
        connectionFactory.addConnectionListener(this);
    }

    @PostConstruct
    public void declareQueuesOnStartup() {
        declareQueues();
    }

    private void declareQueues() {
        for (Queue queue : queuesToDeclare) {
            rabbitAdmin.declareQueue(queue);
        }
    }

    @Override
    public void onCreate(org.springframework.amqp.rabbit.connection.Connection connection) {
        // Called on connection (re)creation - declare queues dynamically
        declareQueues();
    }

    @Override
    public void onClose(org.springframework.amqp.rabbit.connection.Connection connection) {
        // No action required on connection close currently.
        // Add handling here if needed in the future.
    }
        // Add handling here if needed in the future.
}