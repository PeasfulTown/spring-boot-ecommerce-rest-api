package xyz.peasfultown.ecommerce.cart_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<CartEntity, UUID> {
    @Query("""
            select c from CartEntity c where c.userId = :userId
            """)
    Optional<CartEntity> findCartByUserId(UUID userId);
}
