package xyz.peasfultown.ecommerce.product_service.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProductSpecification {
    public static Specification<ProductEntity> hasIdsIn(List<UUID> productIds) {
        return (root, query, cb) ->
        productIds == null ? null : root.get("id").in(productIds);
    }

    public static Specification<ProductEntity> hasName(String name) {
        return (root, query, cb) ->
        name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<ProductEntity> hasCategoryName(String categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null) return null;
            Join<ProductEntity, CategoryEntity> categories = root.join("category", JoinType.INNER);
            return cb.like(categories.get("name"), categoryName);
        };
    }

    public static Specification<ProductEntity> hasPriceLowerThan(BigDecimal maxPrice) {
        return (root, query, cb) ->
        maxPrice == null ? null : cb.lessThan(root.get("price"), maxPrice);
    }

    public static Specification<ProductEntity> hasPriceGreaterThan(BigDecimal minPrice) {
        return (root, query, cb) ->
                minPrice == null ? null : cb.greaterThan(root.get("price"), minPrice);
    }

    public static Specification<ProductEntity> hasStockStatus(List<ProductEntity.StockStatus> stockStatus) {
        return (root, query, cb) -> {
            if (stockStatus == null || stockStatus.isEmpty()) return null;

            return root.get("stockStatus").in(stockStatus);
        };
    }
}
