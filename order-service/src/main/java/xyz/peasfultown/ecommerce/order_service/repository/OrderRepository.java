package xyz.peasfultown.ecommerce.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID>
        , JpaSpecificationExecutor<OrderEntity> {
}
