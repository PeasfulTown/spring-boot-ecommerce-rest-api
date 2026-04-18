package xyz.peasfultown.ecommerce.order_service.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import xyz.peasfultown.ecommerce.order_api.model.OrderStatus;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;

import java.util.UUID;

public class OrderSpecification {
    public static Specification<OrderEntity> hasStatus(String statusStr) {
        return (root, query, cb) ->
        statusStr == null ? null : cb.equal(root.get("status"), OrderEntity.OrderStatus.valueOf(statusStr));
    }

    public static Specification<OrderEntity> hasUserId(UUID userId) {
        return (root, query, cb) ->
        cb.equal(root.get("userId"), userId);
    }
}
