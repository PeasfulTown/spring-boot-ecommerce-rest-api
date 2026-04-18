package xyz.peasfultown.ecommerce.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.peasfultown.ecommerce.order_service.entity.OrderItemEntity;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
}
