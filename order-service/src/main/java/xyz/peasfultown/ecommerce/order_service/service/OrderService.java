package xyz.peasfultown.ecommerce.order_service.service;

import org.springframework.data.domain.Page;
import xyz.peasfultown.ecommerce.order_api.model.Order;

public interface OrderService {
    Page<Order> queryOrders(String userId, String status, Integer page, Integer size);
}
