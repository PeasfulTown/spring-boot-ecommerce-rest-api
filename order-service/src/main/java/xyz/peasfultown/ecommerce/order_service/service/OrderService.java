package xyz.peasfultown.ecommerce.order_service.service;

import org.springframework.data.domain.Page;
import xyz.peasfultown.ecommerce.order_api.model.Order;
import xyz.peasfultown.ecommerce.order_api.model.UpdateOrderStatusReq;
import xyz.peasfultown.ecommerce.order_service.dto.OrderCreateMessage;

import java.util.List;

public interface OrderService {
    Page<Order> queryOrders(String userId, String status, Integer page, Integer size);

    Page<Order> queryOrders(Integer page, Integer size);

    Order getOrderById(String userId, String orderId);

    Page<Order> getUserPagedOrdersByUserIdAndOrderStatus(String userId, String status, Integer page, Integer size);

    List<Order> getOrdersByUserId(String userId);

    void updateOrderStatus(String orderId, UpdateOrderStatusReq updateOrderStatusReq);

    void createOrder(OrderCreateMessage message);
}
