package xyz.peasfultown.ecommerce.inventory_service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.peasfultown.ecommerce.inventory_service.dto.UpdateInventoryStockMessage;
import xyz.peasfultown.ecommerce.inventory_service.service.InventoryService;

@Component
@Slf4j
public class MessageConsumer {
    private final RabbitTemplate rabbitTemplate;
    private final InventoryService invService;

    @Autowired
    public MessageConsumer(RabbitTemplate rabbitTemplate, InventoryService invService) {
        this.rabbitTemplate = rabbitTemplate;
        this.invService = invService;
    }

    @RabbitListener(
            queues = { "#{inventory_stockUpdate_queue.getName}" },
            messageConverter = "jsonConverter"
    )
    public void handleStockUpdateMessage(UpdateInventoryStockMessage message) {
        log.info("=== STOCK UPDATE QUEUE RECEIVED MESSAGE ===");
        invService.updateProductsStocks(message);
    }
}
