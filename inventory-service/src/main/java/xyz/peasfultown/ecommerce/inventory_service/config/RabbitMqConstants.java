package xyz.peasfultown.ecommerce.inventory_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class RabbitMqConstants {
    public static String exchange;
    public static String orderSubmitted_queue = "orders.submitted";

    public String getExchange() {
        return exchange;
    }

    @Value("${spring.rabbitmq.template.default-exchange}")
    public void setExchange(String exchange) {
        RabbitMqConstants.exchange = exchange;
    }
}
