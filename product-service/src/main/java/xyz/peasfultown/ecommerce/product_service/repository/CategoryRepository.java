package xyz.peasfultown.ecommerce.product_service.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    @Query(value = """
            select c from CategoryEntity c where c.name = :name
            """)
    Optional<CategoryEntity> findCategoryByName(@NotNull @Param("name") String name);

    @Query(value = """
            select c.products from CategoryEntity c where c.id = :id
            """)
    List<ProductEntity> findProductsByCategoryId(UUID id);
}
