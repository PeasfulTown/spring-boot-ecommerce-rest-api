package xyz.peasfultown.ecommerce.inventory_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.inventory_api.model.Inventory;
import xyz.peasfultown.ecommerce.inventory_api.model.UpdateInventoryReq;
import xyz.peasfultown.ecommerce.inventory_service.dto.OrderSubmission;
import xyz.peasfultown.ecommerce.inventory_service.entity.InventoryEntity;
import xyz.peasfultown.ecommerce.inventory_service.exception.ProductInventoryNotFoundException;
import xyz.peasfultown.ecommerce.inventory_service.mapper.InventoryMapper;
import xyz.peasfultown.ecommerce.inventory_service.repository.InventoryRepository;
import xyz.peasfultown.ecommerce.inventory_service.repository.InventorySpecification;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository repo;
    private final InventoryMapper mapper;

    @Autowired
    public InventoryServiceImpl(InventoryRepository repo, InventoryMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Page<Inventory> getAllProductInventory(Integer page, Integer size) {
        Pageable pageRequest = PageRequest.of(page, size);
        Page<InventoryEntity> inventoryEntityPage = repo.findAll(pageRequest);
        return inventoryEntityPage.map(mapper::toModel);
    }

    @Override
    public Inventory getProductInventoryByProductId(String productId) {
        InventoryEntity ie = repo.findInventoryByProductId(UUID.fromString(productId))
                .orElseThrow(() -> new ProductInventoryNotFoundException(String.format(
                    "Inventory not found for product ID: %s", productId
                )));

        return mapper.toModel(ie);
    }

    @Override
    public void updateProductInventoryById(String productId, UpdateInventoryReq updateInventoryReq) {
        InventoryEntity ie = repo.findInventoryByProductId(UUID.fromString(productId))
                .orElseThrow(() -> new ProductInventoryNotFoundException(String.format(
                        "Inventory not found for product ID: %s", productId
                )));

        ie.setStock(updateInventoryReq.getStock());
        repo.save(ie);
    }

    @Override
    public Page<Inventory> getLowStockProducts(Integer page, Integer size) {
        Specification<InventoryEntity> isLowStock = InventorySpecification.isLowStock();
        Pageable pageReq = PageRequest.of(page, size);
        Page<InventoryEntity> invEntities = repo.findAll(isLowStock, pageReq);
        return invEntities.map(mapper::toModel);
    }

    @Override
    public Page<Inventory> getOutOfStockProducts(Integer page, Integer size) {
        Specification<InventoryEntity> isOutOfStock = InventorySpecification.isOutOfStock();
        Pageable pageReq = PageRequest.of(page, size);
        Page<InventoryEntity> invEntities = repo.findAll(isOutOfStock, pageReq);
        return invEntities.map(mapper::toModel);
    }

    @RabbitListener(
            queues = { "#{ordersSubmitted_queue.getName}" },
            messageConverter = "jsonConverter"
    )
    public void handleOrderSubmitted(OrderSubmission orderSubmission) {

    }
}
