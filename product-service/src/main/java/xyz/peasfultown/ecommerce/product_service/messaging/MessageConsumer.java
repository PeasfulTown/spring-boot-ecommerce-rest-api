package xyz.peasfultown.ecommerce.product_service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.peasfultown.ecommerce.product_service.dto.ProductStockUpdateMessageDto;
import xyz.peasfultown.ecommerce.product_service.exception.MessageContentEmptyException;
import xyz.peasfultown.ecommerce.product_service.service.ProductService;

@Slf4j
@Component
public class MessageConsumer {
    private final ProductService prodService;

    @Autowired
    public MessageConsumer(ProductService prodService) {
        this.prodService = prodService;
    }

    @RabbitListener(
            queues = "#{product_updateStock_queue.getName}",
            messageConverter = "jsonConverter"
    )
    public void handleStockUpdateMessages(ProductStockUpdateMessageDto dto) {
        log.info("==== RECEIVED STOCK UPDATE MESSAGE ====");
        if (dto.getContent() == null || dto.getContent().isEmpty())
            throw new MessageContentEmptyException("Unable to process stock update message, content is empty");
        prodService.updateProductStock(dto);
    }
}
