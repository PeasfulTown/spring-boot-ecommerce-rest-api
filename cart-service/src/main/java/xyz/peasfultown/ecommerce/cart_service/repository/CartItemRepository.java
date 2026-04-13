package xyz.peasfultown.ecommerce.cart_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;

import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {
}
