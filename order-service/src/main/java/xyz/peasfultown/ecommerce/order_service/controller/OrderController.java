package xyz.peasfultown.ecommerce.order_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.order_api.OrderApi;
import xyz.peasfultown.ecommerce.order_api.model.*;
import xyz.peasfultown.ecommerce.order_service.controller.aspect.AdminOnly;
import xyz.peasfultown.ecommerce.order_service.service.OrderService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class OrderController implements OrderApi {
    private final OrderService service;

    @Autowired
    public OrderController(OrderService service) {
        this.service = service;
    }

    private PagedOrderResponse createPagedOrderResponse(Page<Order> orderPage) {
        return PagedOrderResponse.builder()
                .content(orderPage.getContent())
                .page(new ResponsePage()
                        .number(orderPage.getNumber())
                        .size(orderPage.getSize())
                        .totalElements(orderPage.getTotalElements())
                        .totalPages(orderPage.getTotalPages()))
                .build();
    }

    @Override
    public ResponseEntity<PagedOrderResponse> getAllOrders(String userId, String userRole, OrderStatus status, Integer page, Integer size) throws Exception {
        Page<Order> orders;

        if (userRole.equalsIgnoreCase("ADMIN"))
            orders = service.queryOrders(null, status, page, size);
        else
            orders = service.queryOrders(userId, status, page, size);

        return ok(createPagedOrderResponse(orders));
    }

    @Override
    public ResponseEntity<Order> getOrderById(String userId, String userRole, String orderId) throws Exception {
        Order order;
        if (userRole.equalsIgnoreCase("ADMIN"))
            order = service.getOrderByOrderId(orderId);
        else
            order = service.getOrderByUserIdAndOrderId(userId, orderId);
        return ok(order);
    }

    @AdminOnly
    @Override
    public ResponseEntity<PagedOrderResponse> getOrdersByUserId(String userRole, String userId, OrderStatus orderStatus, Integer pageNumber, Integer pageSize) throws Exception {
        Page<Order> orders = service.queryOrders(userId, orderStatus, pageNumber, pageSize);
        return ok(createPagedOrderResponse(orders));
    }

    @AdminOnly
    @Override
    public ResponseEntity<Void> updateOrderStatus(String userRole, String orderId, OrderUpdateRequest updateRequest) throws Exception {
        service.updateOrderStatus(orderId, updateRequest);
        return status(HttpStatus.NO_CONTENT).build();
    }
}
