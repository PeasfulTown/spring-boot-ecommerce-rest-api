package xyz.peasfultown.ecommerce.order_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Bean
    public Queue orderSubmitted_queue() {
        return new Queue(RabbitMqConstants.orderSubmitted_queue);
    }
}
