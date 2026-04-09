package xyz.peasfultown.ecommerce.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID>
        , JpaSpecificationExecutor<ProductEntity> {
}
