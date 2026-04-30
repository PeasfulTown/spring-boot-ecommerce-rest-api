package xyz.peasfultown.ecommerce.cart_service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import xyz.peasfultown.ecommerce.cart_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.cart_service.dto.OrderCreateMessage;
import xyz.peasfultown.ecommerce.cart_service.exception.CustomErrorResponseException;

@Component
public class MessagePublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper oMapper;

    @Autowired
    public MessagePublisher(RabbitTemplate rabbitTemplate, ObjectMapper oMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.oMapper = oMapper;
    }

    public void sendOrderCreateMessage(OrderCreateMessage messageBody) {
        try {
            Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(messageBody))
                    .setHeader("__TypeId__", OrderCreateMessage.class.getSimpleName())
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
            rabbitTemplate.send(RabbitMqConstants.cart_checkout_order_createOrder_routingKey, message);
        } catch (JsonProcessingException e) {
            throw new CustomErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR,
            "Unable to send OrderCreateMessage as json");
        }
    }
}
