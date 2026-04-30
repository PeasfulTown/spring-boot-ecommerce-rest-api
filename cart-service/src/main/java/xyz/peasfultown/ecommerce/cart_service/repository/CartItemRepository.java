package xyz.peasfultown.ecommerce.cart_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {
    @Query("""
            select ci from CartItemEntity ci
            where ci.cart.id = :cartId
            and ci.productId = :productId
            """)
    Optional<CartItemEntity> findCartItemByCartIdAndProductId(UUID cartId,
                                                              UUID productId);
    @Query("""
            select ci from CartItemEntity ci
            where ci.cart.id = :cartId
            """)
    List<CartItemEntity> findCartItemsByCartId(UUID cartId);

    @Query("""
            select ci from CartItemEntity ci
            where ci.cart.userId = :userId
            """)
    List<CartItemEntity> findCartItemsByUserId(UUID userId);

    @Query("""
            select ci from CartItemEntity ci
            where ci.cart.userId = :userId
            and ci.productId = :productId
            """)
    Optional<CartItemEntity> findCartItemByUserIdAndProductId(UUID userId, UUID productId);
}
