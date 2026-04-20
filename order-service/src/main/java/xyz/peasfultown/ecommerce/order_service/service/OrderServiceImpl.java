package xyz.peasfultown.ecommerce.order_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.order_api.model.CartItem;
import xyz.peasfultown.ecommerce.order_api.model.Order;
import xyz.peasfultown.ecommerce.order_api.model.UpdateOrderStatusReq;
import xyz.peasfultown.ecommerce.order_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.order_service.dto.OrderSubmission;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;
import xyz.peasfultown.ecommerce.order_service.entity.OrderItemEntity;
import xyz.peasfultown.ecommerce.order_service.exception.EmptyCardCheckoutException;
import xyz.peasfultown.ecommerce.order_service.exception.OrderNotFoundException;
import xyz.peasfultown.ecommerce.order_service.mapper.OrderMapper;
import xyz.peasfultown.ecommerce.order_service.repository.OrderItemRepository;
import xyz.peasfultown.ecommerce.order_service.repository.OrderRepository;
import xyz.peasfultown.ecommerce.order_service.repository.specification.OrderSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repo;
    private final OrderItemRepository oiRepo;
    private final OrderMapper mapper;

    @Autowired
    public OrderServiceImpl(OrderRepository repo, OrderItemRepository oiRepo, OrderMapper mapper) {
        this.repo = repo;
        this.oiRepo = oiRepo;
        this.mapper = mapper;
    }

    @Override
    public Page<Order> queryOrders(String userId, String status, Integer page, Integer size) {
        Specification<OrderEntity> hasUserId = OrderSpecification.hasUserId(UUID.fromString(userId));
        Specification<OrderEntity> hasStatus = OrderSpecification.hasStatus(status);
        // TODO: add sort?
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderEntity> oe = repo.findAll(
                hasUserId
                        .and(hasStatus),
                pageable
        );
        return oe.map(mapper::toModel);
    }

    @Override
    public Page<Order> queryOrders(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderEntity> oe = repo.findAll(pageable);
        return oe.map(mapper::toModel);
    }

    @Override
    public Order getOrderById(String userId, String orderId) {
        OrderEntity oe = repo.findOrderByUserIdAndOrderId(UUID.fromString(userId), UUID.fromString(orderId))
                .orElseThrow(() -> new OrderNotFoundException(String.format(
                        "Order not found by ID: %s", orderId
                )));

        return mapper.toModel(oe);
    }

    @Override
    public Page<Order> getUserPagedOrdersByUserIdAndOrderStatus(String userId, String status, Integer page, Integer size) {
        Specification<OrderEntity> hasStatus = OrderSpecification.hasStatus(status);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderEntity> oe = repo.findAll(hasStatus, pageable);
        return oe.map(mapper::toModel);
    }

    @Override
    public List<Order> getOrdersByUserId(String userId) {
        List<OrderEntity> oe = repo.findOrdersByUserId(UUID.fromString(userId));
        return oe.stream().map(mapper::toModel).toList();
    }

    @Override
    public void updateOrderStatus(String orderId, UpdateOrderStatusReq updateOrderStatusReq) {
        OrderEntity oe = repo.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new OrderNotFoundException(String.format(
                        "Order not found by ID: %s", orderId
                )));

        oe.setStatus(OrderEntity.OrderStatus.valueOf(updateOrderStatusReq.getOrderStatus().getValue()));
    }

    @RabbitListener(
            queues = "#{orderSubmitted_queue.getName}",
            messageConverter = "jsonConverter"
    )
    public void consumeSubmittedOrdersQueue(OrderSubmission message) {
        if (message.getItems().size() == 0)
            throw new EmptyCardCheckoutException(
                    "Unable to create order with empty cart"
            );
        OrderEntity oe = OrderEntity.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(message.getUserId()))
                .email(message.getContactEmail())
                .phone(message.getContactPhone())
                .number(message.getAddressNumber())
                .street(message.getAddressStreet())
                .city(message.getAddressCity())
                .state(message.getAddressState())
                .country(message.getAddressCountry())
                .postalCode(message.getAddressPostalCode())
                .totalPrice(message.getOrderTotal())
                .itemCount(message.getOrderItemCount())
                .build();

        for (CartItem i : message.getItems()) {
            OrderItemEntity oie = OrderItemEntity.builder()
                    .id(UUID.randomUUID())
                    .order(oe)
                    .productId(UUID.fromString(i.getProductId()))
                    .productName(i.getProductName())
                    .productPrice(i.getProductPrice())
                    .quantity(i.getQuantity())
                    .subtotal(i.getSubtotal())
                .build();
            oe.getItems().add(oie);
        }
        repo.save(oe);
    }
}
