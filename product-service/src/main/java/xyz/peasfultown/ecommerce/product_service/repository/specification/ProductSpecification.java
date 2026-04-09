package xyz.peasfultown.ecommerce.product_service.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;

public class ProductSpecification {
    public static Specification<ProductEntity> hasName(String name) {
        return (root, query, cb) ->
        name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<ProductEntity> hasCategoryName(String categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null) return null;
            Join<ProductEntity, CategoryEntity> categories = root.join("categories", JoinType.INNER);
            return cb.like(categories.get("name"), categoryName);
        };
    }
}
