package xyz.peasfultown.ecommerce.inventory_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConstants {
    public static String exchange;

    public static String cart_checkout_inventory_updateStock_routingKey = "cart.checkout.inventory.update-stock";

    public static String inventory_updateStock_product_updateStockStatus_routingKey = "cart.checkout.product.update-stock-status";

    public static String inventory_stockUpdate_queue = "inventory.stock-update.queue";

    public static String product_stockStatusUpdate_queue = "product.stock-status-update.queue";

    public String getExchange() {
        return exchange;
    }

    @Value("${spring.rabbitmq.template.default-exchange}")
    public void setExchange(String exchange) {
        RabbitMqConstants.exchange = exchange;
    }
}
