package xyz.peasfultown.ecommerce.product_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ValueMapping;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_api.model.StockStatus;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductEntity modelToEntity(Product model);

    @Mapping(target = "stockStatus", source = "stock", qualifiedByName = "stockNumberToStockStatus")
    Product toModel(ProductEntity entity);

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

    default UUID map(String uuid) {
        if (uuid == null) return null;
        return UUID.fromString(uuid);
    }

    @Named("stockNumberToStockStatus")
    default StockStatus stockNumberToStockStatus(Integer stock) {
        if (stock == 0) return StockStatus.OUT_OF_STOCK;
        else if (stock <= 20) return StockStatus.LOW_STOCK;
        else return StockStatus.IN_STOCK;
    }


}
