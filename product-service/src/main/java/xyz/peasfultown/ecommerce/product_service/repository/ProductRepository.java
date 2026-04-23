package xyz.peasfultown.ecommerce.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID>
        , JpaSpecificationExecutor<ProductEntity> {
    @Query("""
            SELECT p FROM ProductEntity p 
            WHERE p.createdAt = (SELECT MAX(p2.createdAt) FROM ProductEntity p2)
            """)
    ProductEntity findTop();

    @Query("""
            SELECT p FROM ProductEntity p
            WHERE p.id NOT IN :ids
            """)
    List<ProductEntity> findProductsWithIdsNotIn(List<UUID> ids);
}
