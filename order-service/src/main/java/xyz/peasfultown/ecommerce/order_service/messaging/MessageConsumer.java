package xyz.peasfultown.ecommerce.order_service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.peasfultown.ecommerce.order_service.dto.OrderCreateMessage;
import xyz.peasfultown.ecommerce.order_service.service.OrderService;

@Slf4j
@Component
public class MessageConsumer {
    private final OrderService orderService;

    @Autowired
    public MessageConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(
            queues = "#{order_createOrder_queue.getName}",
            messageConverter = "jsonConverter"
    )
    public void consumeSubmittedOrdersQueue(OrderCreateMessage message) {
        log.info("==== ORDER CREATE MESSAGE RECEIVED ====");
        orderService.createOrder(message);
    }
}
