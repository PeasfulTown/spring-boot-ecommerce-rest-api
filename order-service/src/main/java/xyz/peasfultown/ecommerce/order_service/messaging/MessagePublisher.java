package xyz.peasfultown.ecommerce.order_service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import xyz.peasfultown.ecommerce.order_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.order_service.dto.PaymentConfirmationMessage;
import xyz.peasfultown.ecommerce.order_service.exception.CustomErrorResponseException;

@Component
public class MessagePublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper oMapper;

    @Autowired
    public MessagePublisher(RabbitTemplate rabbitTemplate, ObjectMapper oMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.oMapper = oMapper;
    }

    public void sendPaymentConfirmationMessage(PaymentConfirmationMessage paymentConfirmationMessage) {
        try {
            Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(paymentConfirmationMessage))
                    .setHeader("__TypeId__", PaymentConfirmationMessage.class.getSimpleName())
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
            rabbitTemplate.send(RabbitMqConstants.cart_checkout_payment_confirmPayment_routingKey, message);
        } catch (JsonProcessingException e) {
            throw new CustomErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR,
            "Unable to process rabbitmq message with json ObjectMapper");

        }

    }
}
