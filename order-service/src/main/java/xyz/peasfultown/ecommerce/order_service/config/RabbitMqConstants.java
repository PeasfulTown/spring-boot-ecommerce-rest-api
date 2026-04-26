package xyz.peasfultown.ecommerce.order_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConstants {
    public static final String TYPEID_HEADER = "__TypeId__";
    public static String exchange;
    public static String order_createOrder_queue = "order.create-order.queue";
    public static String order_confirmOrder_queue = "order.confirm-order.queue";
    public static String order_cancelOrder_queue = "order.cancel-order.queue";
    public static String payment_confirmPayment_queue = "payment.confirm-payment.queue";
    public static String product_updateStock_queue = "product.update-stock.queue";

    public static String cart_checkout_order_createOrder_routingKey = "cart.checkout.order.create-order";
    public static String cart_checkout_order_confirmOrder_routingKey = "cart.checkout.order.confirm-order";
    public static String cart_checkout_order_cancelOrder_routingKey = "cart.checkout.order.cancel-order";
    public static String cart_checkout_payment_confirmPayment_routingKey = "cart.checkout.payment.confirm-payment";
    public static String cart_checkout_product_updateStock_routingKey = "cart.checkout.product.update-stock";

    public String getExchange() {
        return exchange;
    }

    @Value("${spring.rabbitmq.template.default-exchange}")
    public void setExchange(String exchange) {
        RabbitMqConstants.exchange = exchange;
    }

}
