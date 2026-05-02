package xyz.peasfultown.ecommerce.payment_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqConstants {
    public static String exchange;
    public static String payment_confirmPayment_queue = "payment.confirm-payment.queue";
    public static String order_confirmOrder_queue = "order.confirm-order.queue";
    public static String order_cancelOrder_queue = "order.cancel-order.queue";

    public static String cart_checkout_payment_confirmPayment_routingKey = "cart.checkout.payment.confirm-payment";
    public static String cart_checkout_order_confirmOrder_routingKey = "cart.checkout.order.confirm-order";
    public static String cart_checkout_order_cancelOrder_routingKey = "cart.checkout.order.cancel-order";

    public String getExchange() {
        return exchange;
    }

    @Value("${spring.rabbitmq.template.default-exchange}")
    public void setExchange(String exchange) {
        RabbitMqConstants.exchange = exchange;
    }
}
