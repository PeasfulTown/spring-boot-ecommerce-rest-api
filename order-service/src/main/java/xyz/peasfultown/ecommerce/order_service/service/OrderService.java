package xyz.peasfultown.ecommerce.order_service.service;

import org.springframework.data.domain.Page;
import xyz.peasfultown.ecommerce.order_api.model.Order;
import xyz.peasfultown.ecommerce.order_api.model.OrderStatus;
import xyz.peasfultown.ecommerce.order_api.model.OrderUpdateRequest;
import xyz.peasfultown.ecommerce.order_service.dto.OrderConfirmationMessage;
import xyz.peasfultown.ecommerce.order_service.dto.OrderCreateMessage;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;

import java.util.List;

public interface OrderService {
    Page<Order> queryOrders(String userId, OrderStatus status, Integer page, Integer size);

    Order getOrderByUserIdAndOrderId(String userId, String orderId);

    Order getOrderByOrderId(String orderId);

    void updateOrderStatus(String orderId, OrderUpdateRequest updateRequest);

    Order createOrder(OrderCreateMessage message);

    void confirmOrder(OrderConfirmationMessage message);
}
