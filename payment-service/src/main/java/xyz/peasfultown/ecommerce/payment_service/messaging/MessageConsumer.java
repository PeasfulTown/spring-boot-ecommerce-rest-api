package xyz.peasfultown.ecommerce.payment_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.peasfultown.ecommerce.payment_service.dto.PaymentConfirmationMessage;
import xyz.peasfultown.ecommerce.payment_service.service.PaymentService;

@Component
public class MessageConsumer {
    private final RabbitTemplate rabbitTemplate;
    private final PaymentService paymentService;

    @Autowired
    public MessageConsumer(RabbitTemplate rabbitTemplate, PaymentService paymentService) {
        this.rabbitTemplate = rabbitTemplate;
        this.paymentService = paymentService;
    }

    @RabbitListener(
            queues = "#{payment_confirmPayment_queue.getName}",
            messageConverter = "jsonConverter"
    )
    public void handlePaymentConfirmationMessage(PaymentConfirmationMessage message) {
        paymentService.validatePayment(message);
    }
}
