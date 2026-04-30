package xyz.peasfultown.ecommerce.cart_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConstants {
    public static String exchange;
    public static String order_createOrder_queue = "order.create-order.queue";
    public static String cart_checkout_order_createOrder_routingKey = "cart.checkout.order.create-order";

    @Value("${spring.rabbitmq.template.default-exchange}")
    public void setExchange(String exchange) {
        RabbitMqConstants.exchange = exchange;
    }

    public String getExchange() {
        return exchange;
    }
}
