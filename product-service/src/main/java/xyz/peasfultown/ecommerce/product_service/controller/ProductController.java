package xyz.peasfultown.ecommerce.product_service.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.product_api.ProductApi;
import xyz.peasfultown.ecommerce.product_api.model.*;
import xyz.peasfultown.ecommerce.product_service.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class ProductController implements ProductApi {
    private final ProductService service;

    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<PagedProductResponse> queryProducts(String name,
                                                               String category,
                                                               BigDecimal minPrice,
                                                               BigDecimal maxPrice,
                                                               List<ProductStockStatus> stockStatus,
                                                               String sortBy,
                                                               String sortDir,
                                                               Integer page,
                                                               Integer size) throws Exception {
        Page<Product> products = service.queryProducts(name, category, minPrice, maxPrice, stockStatus, sortBy, sortDir, page, size);
        PagedProductResponse response = new PagedProductResponse();
        response.content(products.getContent())
                .page(new PagedResponsePage()
                        .size(products.getSize())
                        .number(products.getNumber())
                        .totalElements(products.getTotalElements())
                        .totalPages(products.getTotalPages())
                );
        return ok(response);
    }

    @Override
    public ResponseEntity<Product> addProduct(@Valid NewProductReq newProductReq) throws Exception {
        return status(HttpStatus.CREATED).body(service.createProduct(newProductReq));
    }

    @Override
    public ResponseEntity<Void> deleteProductById(String id) throws Exception {
        service.deleteProductById(id);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Product> getProductById(String id) throws Exception {
        return ok(service.getProductById(id));
    }
}
