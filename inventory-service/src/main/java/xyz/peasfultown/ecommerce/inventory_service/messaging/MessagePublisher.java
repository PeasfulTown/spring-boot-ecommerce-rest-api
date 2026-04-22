package xyz.peasfultown.ecommerce.inventory_service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.peasfultown.ecommerce.inventory_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.inventory_service.dto.UpdateProductStockStatusMessage;

@Component
public class MessagePublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper oMapper;

    @Autowired
    public MessagePublisher(RabbitTemplate rabbitTemplate, ObjectMapper oMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.oMapper = oMapper;
    }

    public void sendUpdateProductStockMessage(UpdateProductStockStatusMessage messageDto) {
        try {

            Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(messageDto))
                            .setHeader("__TypeId__", "ProductIdStockMap")
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
            rabbitTemplate.send(RabbitMqConstants.inventory_updateStock_product_updateStockStatus_routingKey, message);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to send product stock update message: unable to write message as json");
        }
    }
}
