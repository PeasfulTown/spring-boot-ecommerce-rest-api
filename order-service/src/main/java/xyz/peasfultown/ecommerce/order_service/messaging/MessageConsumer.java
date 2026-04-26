package xyz.peasfultown.ecommerce.order_service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.peasfultown.ecommerce.order_api.model.Order;
import xyz.peasfultown.ecommerce.order_api.model.OrderItem;
import xyz.peasfultown.ecommerce.order_api.model.OrderUpdateRequest;
import xyz.peasfultown.ecommerce.order_service.dto.*;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;
import xyz.peasfultown.ecommerce.order_service.service.OrderService;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MessageConsumer {
    private final OrderService orderService;
    private final MessagePublisher messagePublisher;

    @Autowired
    public MessageConsumer(OrderService orderService, MessagePublisher messagePublisher) {
        this.orderService = orderService;
        this.messagePublisher = messagePublisher;
    }

    @RabbitListener(
            queues = "#{order_createOrder_queue.getName}",
            messageConverter = "jsonConverter"
    )
    public void consumeSubmittedOrdersQueue(OrderCreateMessage message) {
        // create order record in database and then send out a message to
        // payment service to confirm the payment for the order
        log.info("==== ORDER CREATE MESSAGE RECEIVED ====");
        Order order = orderService.createOrder(message);

        messagePublisher.sendPaymentConfirmationMessage(new PaymentConfirmationMessage(order.getId(), message.getCardId()));
    }

    @RabbitListener(
        queues = "#{order_confirmOrder_queue.getName}",
        messageConverter = "jsonConverter"
    )
    public void consumeConfirmOrderMessage(OrderConfirmationMessage message) {
        log.info("==== ORDER CONFIRMATION MESSAGE RECEIVED ====");

        // payment service sends order confirmation message, order service
        // updates the order status to confirmed, and send a message to
        // product service to update its stock
        orderService.confirmOrder(message);
        Order order = orderService.getOrderByOrderId(message.getOrderId());
        Map<String, Integer> productIdStockMap = order.getItems().stream()
                        .collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));
        messagePublisher.sendProductStockUpdateMessage(new ProductStockUpdateMessage(productIdStockMap));
    }

    @RabbitListener(
            queues = "#{order_cancelOrder_queue.getName}",
            messageConverter = "jsonConverter"
    )
    public void consumCancelOrderMessage(OrderCancellationMessage message) {
        log.info("==== ORDER CONFIRMATION MESSAGE RECEIVED ====");

        orderService.cancelOrder(message);

    }
}
