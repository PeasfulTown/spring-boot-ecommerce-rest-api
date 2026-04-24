package xyz.peasfultown.ecommerce.order_service.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import xyz.peasfultown.ecommerce.order_api.model.OrderStatus;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;

import java.util.UUID;

public class OrderSpecification {
    public static Specification<OrderEntity> hasStatus(OrderEntity.OrderStatus status) {
        return (root, query, cb) ->
        status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<OrderEntity> hasUserId(UUID userId) {
        return (root, query, cb) ->
        userId == null ? null : cb.equal(root.get("userId"), userId);
    }
}
