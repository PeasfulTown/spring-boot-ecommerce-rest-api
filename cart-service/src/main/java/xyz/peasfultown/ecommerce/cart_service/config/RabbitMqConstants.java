package xyz.peasfultown.ecommerce.cart_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConstants {
    public static String exchange;
    public static String orderSubmitted_queue = "orders.submitted";
    public static String bindingKey = "order.submit";

    @Value("${spring.rabbitmq.template.default-exchange}")
    public void setExchange(String exchange) {
        RabbitMqConstants.exchange = exchange;
    }

    public void setQueue(String queue) {
        RabbitMqConstants.orderSubmitted_queue = queue;
    }

    public void setBindingKey(String bindingKey) {
        RabbitMqConstants.bindingKey = bindingKey;
    }

    public static String getExchange() {
        return exchange;
    }

    public static String getOrderSubmitted_queue() {
        return orderSubmitted_queue;
    }

    public static String getBindingKey() {
        return bindingKey;
    }

}
