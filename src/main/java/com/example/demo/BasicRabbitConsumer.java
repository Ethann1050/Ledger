package com.example.demo;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BasicRabbitConsumer {

    @RabbitListener(queues = RabbitMQConfig.DEVICE_QUEUE)
    public void procesDeviceNotification(Outbox outbox) {
        System.out.println("Received Outbox Notification for ID: " + outbox.getId());}
    };
