package xyz.peasfultown.ecommerce.order_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConstants {
    public static String exchange;
    public static String orderSubmitted_queue = "order.submitted";
    public static String orderSubmitted_routingKey = "order.submitted";

    public String getExchange() {
        return exchange;
    }

    @Value("${spring.rabbitmq.template.default-exchange}")
    public void setExchange(String exchange) {
        RabbitMqConstants.exchange = exchange;
    }


}
