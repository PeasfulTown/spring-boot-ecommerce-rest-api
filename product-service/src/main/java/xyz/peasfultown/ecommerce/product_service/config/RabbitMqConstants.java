package xyz.peasfultown.ecommerce.product_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConstants {
    public static final String TYPEID_HEADER = "__TypeId__";
    public static String exchange;
    public static String product_updateStock_queue = "product.update-stock.queue";
    public static String cart_checkout_product_updateStock_routingKey = "cart.checkout.product.update-stock";

    @Value("${spring.rabbitmq.template.default-exchange}")
    public void setExchange(String exchange) {
        RabbitMqConstants.exchange = exchange;
    }


}
