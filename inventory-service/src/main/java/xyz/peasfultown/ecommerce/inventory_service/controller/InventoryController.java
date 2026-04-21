package xyz.peasfultown.ecommerce.inventory_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.inventory_api.InventoryApi;
import xyz.peasfultown.ecommerce.inventory_api.model.Inventory;
import xyz.peasfultown.ecommerce.inventory_api.model.PagedInventoryResponse;
import xyz.peasfultown.ecommerce.inventory_api.model.ResponsePage;
import xyz.peasfultown.ecommerce.inventory_api.model.UpdateInventoryReq;
import xyz.peasfultown.ecommerce.inventory_service.controller.aspect.AdminOnly;
import xyz.peasfultown.ecommerce.inventory_service.service.InventoryService;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class InventoryController implements InventoryApi {
    private final InventoryService service;

    @Autowired
    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<PagedInventoryResponse> getAllProductInventory(Integer page, Integer size) throws Exception {
        Page<Inventory> invPage = service.getAllProductInventory(page, size);
        PagedInventoryResponse res = PagedInventoryResponse.builder()
                .content(invPage.getContent())
                .page(ResponsePage.builder()
                        .number(invPage.getNumber())
                        .size(invPage.getSize())
                        .totalElements(invPage.getTotalElements())
                        .totalPages(invPage.getTotalPages())
                        .build())
                .build();
        return ok(res);
    }

    @Override
    public ResponseEntity<Inventory> getProductInventoryByProductId(String productId) throws Exception {
        return ok(service.getProductInventoryByProductId(productId));
    }

    @AdminOnly
    @Override
    public ResponseEntity<Void> updateProductInventoryById(String userRole, String productId, UpdateInventoryReq updateInventoryReq) throws Exception {
        service.updateProductInventoryById(productId, updateInventoryReq);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @AdminOnly
    @Override
    public ResponseEntity<PagedInventoryResponse> getLowStockProducts(String userRole, Integer page, Integer size) throws Exception {
        Page<Inventory> invPage = service.getLowStockProducts(page, size);
        PagedInventoryResponse res = PagedInventoryResponse.builder()
                .content(invPage.getContent())
                .page(ResponsePage.builder()
                        .number(page)
                        .size(size)
                        .totalElements(invPage.getTotalElements())
                        .totalPages(invPage.getTotalPages())
                .build())
        .build();
        return ok(res);
    }

    @AdminOnly
    @Override
    public ResponseEntity<PagedInventoryResponse> getOutOfStockProducts(String xUserRole, Integer page, Integer size) throws Exception {
        Page<Inventory> invPage = service.getOutOfStockProducts(page, size);
        PagedInventoryResponse res = PagedInventoryResponse.builder()
                .content(invPage.getContent())
                .page(ResponsePage.builder()
                        .number(page)
                        .size(size)
                        .totalElements(invPage.getTotalElements())
                        .totalPages(invPage.getTotalPages())
                        .build())
                .build();
        return ok(res);
    }
}
