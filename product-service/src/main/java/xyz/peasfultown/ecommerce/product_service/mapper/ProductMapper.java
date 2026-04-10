package xyz.peasfultown.ecommerce.product_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductEntity modelToEntity(Product model);

    Product entityToModel(ProductEntity entity);

    List<Product> entityListToModelList(List<ProductEntity> pl);

    List<ProductEntity> modelListToEntityList(List<Product> pl);

    default OffsetDateTime map(Instant instant) {
        if (instant == null) return null;
        return instant.atOffset(ZoneOffset.UTC);
    }

    default Instant map(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) return null;
        return offsetDateTime.toInstant();
    }

    default String map(UUID uuid) {
        if (uuid == null) return null;
        return uuid.toString();
    }

    default UUID map (String uuid) {
        if (uuid == null) return null;
        return UUID.fromString(uuid);
    }
}
