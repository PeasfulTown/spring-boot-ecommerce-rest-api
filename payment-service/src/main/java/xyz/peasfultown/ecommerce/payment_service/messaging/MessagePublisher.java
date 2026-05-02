package xyz.peasfultown.ecommerce.payment_service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.peasfultown.ecommerce.payment_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.payment_service.dto.OrderCancellationMessage;
import xyz.peasfultown.ecommerce.payment_service.dto.OrderConfirmationMessage;

@Component
public class MessagePublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper oMapper;

    @Autowired
    public MessagePublisher(RabbitTemplate rabbitTemplate, ObjectMapper oMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.oMapper = oMapper;
    }

    public void sendOrderConfirmationMessage(OrderConfirmationMessage ocm) {
        try {
            Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(ocm))
                    .setHeader("__TypeId__", OrderConfirmationMessage.class.getSimpleName())
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
            rabbitTemplate.send(RabbitMqConstants.cart_checkout_order_confirmOrder_routingKey, message);
        } catch (JsonProcessingException e) {
            // TODO: finish
        }
    }

    public void sendOrderCancellationMessage(OrderCancellationMessage orderCancellationMessage) {
        try {
            Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(orderCancellationMessage))
                    .setHeader("__TypeId__", OrderCancellationMessage.class.getSimpleName())
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
            rabbitTemplate.send(RabbitMqConstants.cart_checkout_order_cancelOrder_routingKey, message);
        } catch (JsonProcessingException e) {
            // TODO: finish
        }
    }
}
