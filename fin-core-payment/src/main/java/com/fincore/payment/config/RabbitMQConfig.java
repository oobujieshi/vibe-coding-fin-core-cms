package com.fincore.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String APPROVAL_EXCHANGE = "fincore.approval.exchange";
    public static final String APPROVAL_QUEUE = "fincore.approval.queue";
    public static final String APPROVAL_ROUTING_KEY = "approval.notify";

    @Bean
    public TopicExchange approvalExchange() {
        return new TopicExchange(APPROVAL_EXCHANGE, true, false);
    }

    @Bean
    public Queue approvalQueue() {
        return QueueBuilder.durable(APPROVAL_QUEUE).build();
    }

    @Bean
    public Binding approvalBinding() {
        return BindingBuilder.bind(approvalQueue()).to(approvalExchange()).with(APPROVAL_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }
}
