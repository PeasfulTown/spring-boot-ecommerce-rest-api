package xyz.peasfultown.ecommerce.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.peasfultown.ecommerce.order_service.entity.OrderItemEntity;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
    @Query("""
            SELECT o FROM OrderItemEntity o WHERE o.order.id = :orderId
            """)
    List<OrderItemEntity> findOrderItemsByOrderId(UUID orderId);
}
