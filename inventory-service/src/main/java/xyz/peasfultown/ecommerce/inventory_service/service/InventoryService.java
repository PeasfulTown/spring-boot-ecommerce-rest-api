package xyz.peasfultown.ecommerce.inventory_service.service;

import org.springframework.data.domain.Page;
import xyz.peasfultown.ecommerce.inventory_api.model.Inventory;
import xyz.peasfultown.ecommerce.inventory_api.model.UpdateInventoryReq;

public interface InventoryService {
    Page<Inventory> getAllProductInventory(Integer page, Integer size);

    Inventory getProductInventoryByProductId(String productId);

    void updateProductInventoryById(String productId, UpdateInventoryReq updateInventoryReq);

    Page<Inventory> getLowStockProducts(Integer page, Integer size);

    Page<Inventory> getOutOfStockProducts(Integer page, Integer size);
}
