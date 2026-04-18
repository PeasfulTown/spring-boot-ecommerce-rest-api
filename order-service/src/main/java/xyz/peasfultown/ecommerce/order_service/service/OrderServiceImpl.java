package xyz.peasfultown.ecommerce.order_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.order_api.model.Order;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;
import xyz.peasfultown.ecommerce.order_service.mapper.OrderMapper;
import xyz.peasfultown.ecommerce.order_service.repository.OrderRepository;
import xyz.peasfultown.ecommerce.order_service.repository.specification.OrderSpecification;

import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repo;
    private final OrderMapper mapper;

    @Autowired
    public OrderServiceImpl(OrderRepository repo, OrderMapper mapper) {
        this.repo = repo;
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
}
