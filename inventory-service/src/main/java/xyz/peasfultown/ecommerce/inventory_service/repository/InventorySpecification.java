package xyz.peasfultown.ecommerce.inventory_service.repository;

import org.springframework.data.jpa.domain.Specification;
import xyz.peasfultown.ecommerce.inventory_service.entity.InventoryEntity;

public class InventorySpecification {
    public static Specification<InventoryEntity> isLowStock() {
        return (root, query, cb) ->
        cb.lessThanOrEqualTo(root.get("stock"), 10);
    }

    public static Specification<InventoryEntity> isOutOfStock() {
        return (root, query, cb) ->
                cb.equal(root.get("stock"), 0);
    }
}
