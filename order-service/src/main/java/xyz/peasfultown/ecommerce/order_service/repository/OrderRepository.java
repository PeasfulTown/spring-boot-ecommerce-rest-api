package xyz.peasfultown.ecommerce.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID>
        , JpaSpecificationExecutor<OrderEntity> {
    @Query("""
            select o from OrderEntity o
            where o.userId = :userId
            and o.id = :orderId
            """)
    Optional<OrderEntity> findOrderByUserIdAndOrderId(UUID userId, UUID orderId);

    @Query("""
            select o from OrderEntity o 
            where o.userId = :userId
            """)
    List<OrderEntity> findOrdersByUserId(@Param("userId") UUID userId);
}
