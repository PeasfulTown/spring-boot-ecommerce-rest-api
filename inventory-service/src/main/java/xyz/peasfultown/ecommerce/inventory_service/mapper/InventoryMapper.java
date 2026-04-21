package xyz.peasfultown.ecommerce.inventory_service.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.ecommerce.inventory_api.model.Inventory;
import xyz.peasfultown.ecommerce.inventory_service.entity.InventoryEntity;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    Inventory toModel(InventoryEntity entity);
}
