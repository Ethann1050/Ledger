package com.example.demo;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIF_EXCHANGE="notifications.exchange";
    public static final String DEVICE_QUEUE="device.queue";

    @Bean
    public DirectExchange notificationExchange(){
        return new DirectExchange(NOTIF_EXCHANGE);
    }

    @Bean
    public Queue deviceQueue(){
        return new Queue(DEVICE_QUEUE, true);
    }

    @Bean
    public Binding deviceBinding(Queue deviceQueue, DirectExchange notificationExchange){
        return BindingBuilder.bind(deviceQueue).to(notificationExchange).with("send.device");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
