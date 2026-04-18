package xyz.peasfultown.ecommerce.order_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.order_api.OrderApi;
import xyz.peasfultown.ecommerce.order_api.model.Order;
import xyz.peasfultown.ecommerce.order_api.model.PagedOrderResponse;
import xyz.peasfultown.ecommerce.order_api.model.ResponsePage;
import xyz.peasfultown.ecommerce.order_service.service.OrderService;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class OrderController implements OrderApi {
    private final OrderService service;

    @Autowired
    public OrderController(OrderService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<PagedOrderResponse> getMyOrders(String userId, String status, Integer page, Integer size) throws Exception {
        Page<Order> orders = service.queryOrders(userId, status, page, size);
        PagedOrderResponse response = new PagedOrderResponse();
        response.content(orders.getContent())
                .page(new ResponsePage()
                        .number(orders.getNumber())
                        .size(orders.getSize())
                        .totalElements(orders.getTotalElements())
                        .totalPages(orders.getTotalPages()));

        return ok(response);
    }
}
