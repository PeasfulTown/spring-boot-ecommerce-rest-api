package xyz.peasfultown.ecommerce.order_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.ecommerce.order_api.model.OrderItem;
import xyz.peasfultown.ecommerce.order_api.model.Order;
import xyz.peasfultown.ecommerce.order_api.model.OrderStatus;
import xyz.peasfultown.ecommerce.order_api.model.OrderUpdateRequest;
import xyz.peasfultown.ecommerce.order_service.client.UserServiceClient;
import xyz.peasfultown.ecommerce.order_service.dto.*;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;
import xyz.peasfultown.ecommerce.order_service.entity.OrderItemEntity;
import xyz.peasfultown.ecommerce.order_service.exception.CustomErrorResponseException;
import xyz.peasfultown.ecommerce.order_service.exception.OrderNotFoundException;
import xyz.peasfultown.ecommerce.order_service.mapper.OrderMapper;
import xyz.peasfultown.ecommerce.order_service.repository.OrderItemRepository;
import xyz.peasfultown.ecommerce.order_service.repository.OrderRepository;
import xyz.peasfultown.ecommerce.order_service.repository.specification.OrderSpecification;

import java.util.UUID;

@Slf4j
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repo;
    private final OrderItemRepository oiRepo;
    private final OrderMapper mapper;
    private final UserServiceClient userClient;

    @Autowired
    public OrderServiceImpl(OrderRepository repo, OrderItemRepository oiRepo, OrderMapper mapper, UserServiceClient userClient) {
        this.repo = repo;
        this.oiRepo = oiRepo;
        this.mapper = mapper;
        this.userClient = userClient;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> queryOrders(String userId, OrderStatus status, Integer pageNumber, Integer pageSize) {
        Specification<OrderEntity> hasUserId = OrderSpecification.hasUserId(
                userId == null ? null : UUID.fromString(userId));
        Specification<OrderEntity> hasStatus = OrderSpecification.hasStatus(
                status == null ? null : OrderEntity.OrderStatus.fromValue(status.getValue()));

        // TODO: add sort?
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<OrderEntity> oe = repo.findAll(
                hasUserId
                        .and(hasStatus),
                pageable
        );
        return oe.map(mapper::toModel);
    }

    @Override
    public Order getOrderByOrderId(String orderId) {
        OrderEntity oe = repo.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new OrderNotFoundException(String.format(
                        "Order not found by ID: %s", orderId
                )));

        return mapper.toModel(oe);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByUserIdAndOrderId(String userId, String orderId) {
        OrderEntity oe = repo.findOrderByUserIdAndOrderId(UUID.fromString(userId), UUID.fromString(orderId))
                .orElseThrow(() -> new OrderNotFoundException(String.format(
                        "Order not found by ID: %s", orderId
                )));

        return mapper.toModel(oe);
    }

    @Override
    public void updateOrderStatus(String orderId, OrderUpdateRequest updateRequest) {
        OrderEntity oe = repo.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new OrderNotFoundException(String.format(
                        "Order not found by ID: %s", orderId
                )));

        oe.setStatus(OrderEntity.OrderStatus.valueOf(updateRequest.getOrderStatus().getValue()));
        repo.save(oe);
    }

    @Override
    public Order createOrder(OrderCreateMessage message) {
        OrderInformationRequest req = OrderInformationRequest.builder()
                .userId(message.getUserId())
                .addressId(message.getAddressId())
            .build();

        try {
            OrderInformation orderInformation = userClient.getOrderInformation(req);

            OrderEntity oe = OrderEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(UUID.fromString(orderInformation.getUserId()))
                    .fullName(orderInformation.getFullName())
                    .email(orderInformation.getEmail())
                    .phone(orderInformation.getPhone())
                    .streetNumber(orderInformation.getAddress().getStreetNumber())
                    .streetName(orderInformation.getAddress().getStreetName())
                    .city(orderInformation.getAddress().getCity())
                    .state(orderInformation.getAddress().getState())
                    .country(orderInformation.getAddress().getCountry())
                    .postalCode(orderInformation.getAddress().getPostalCode())
                    .totalPrice(message.getTotalPrice())
                    .status(OrderEntity.OrderStatus.PROCESSING)
                    .itemCount(message.getItemCount())
                    .build();

            for (OrderItem i : message.getItems()) {
                OrderItemEntity oie = OrderItemEntity.builder()
                        .id(UUID.randomUUID())
                        .productId(UUID.fromString(i.getProductId()))
                        .productName(i.getProductName())
                        .productPrice(i.getProductPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .build();
                oe.addItem(oie);
            }

            return mapper.toModel(repo.save(oe));
        } catch (FeignUserServiceNotFoundException e) {
            throw new CustomErrorResponseException(HttpStatus.BAD_REQUEST,
                    "Unable to create order, query to user service returned not found");
        }
    }

    @Override
    public void confirmOrder(OrderConfirmationMessage message) {
        OrderEntity oe = repo.findById(UUID.fromString(message.getOrderId()))
                .orElseThrow(() -> new OrderNotFoundException(String.format(
                        "Unable to confirm order, order not found by ID: %s", message.getOrderId()
                )));

        oe.setStatus(OrderEntity.OrderStatus.CONFIRMED);
        oe.setPaymentId(UUID.fromString(message.getPaymentId()));
        oe.setPaidAt(message.getPaidAt().toInstant());

        repo.save(oe);
    }

    @Override
    public void cancelOrder(OrderCancellationMessage message) {
        OrderEntity oe = repo.findById(UUID.fromString(message.getOrderId()))
                .orElseThrow(() -> new OrderNotFoundException(String.format(
                        "Unable to cancel order, order not found by ID: %s", message.getOrderId()
                )));

        oe.setStatus(OrderEntity.OrderStatus.CANCELLED);
        repo.save(oe);
    }
}
