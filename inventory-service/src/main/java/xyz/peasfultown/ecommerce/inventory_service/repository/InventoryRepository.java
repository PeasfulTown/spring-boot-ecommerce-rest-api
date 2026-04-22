package xyz.peasfultown.ecommerce.inventory_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import xyz.peasfultown.ecommerce.inventory_service.entity.InventoryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID>
        , JpaSpecificationExecutor<InventoryEntity> {
    @Query("""
            SELECT i FROM InventoryEntity i WHERE i.productId = :productId
            """)
    Optional<InventoryEntity> findInventoryByProductId(UUID productId);

    @Query("""
            SELECT i FROM InventoryEntity i WHERE i.productId IN (:productIds)
            """)
    List<InventoryEntity> findInventoriesByProductIds(List<UUID> productIds);
}
