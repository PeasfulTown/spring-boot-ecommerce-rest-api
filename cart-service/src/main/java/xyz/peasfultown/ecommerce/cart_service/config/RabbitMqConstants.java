package xyz.peasfultown.ecommerce.cart_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConstants {
    public static String exchange;
    public static String order_createOrder_queue = "order.create-order.queue";
    public static String cart_checkout_routingKey = "cart.checkout.#";

    @Value("${spring.rabbitmq.template.default-exchange}")
    public void setExchange(String exchange) {
        RabbitMqConstants.exchange = exchange;
    }

    public void setQueue(String queue) {
        RabbitMqConstants.order_createOrder_queue = queue;
    }

    public void setBindingKey(String bindingKey) {
        RabbitMqConstants.cart_checkout_routingKey = bindingKey;
    }

    public String getExchange() {
        return exchange;
    }

    public String getOrderSubmitted_queue() {
        return order_createOrder_queue;
    }

    public String getBindingKey() {
        return cart_checkout_routingKey;
    }

}
