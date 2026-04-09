package xyz.peasfultown.ecommerce.product_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.product_api.ProductApi;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_service.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class ProductController implements ProductApi {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<Product>> queryProducts(String name,
                                                       String category,
                                                       BigDecimal minPrice,
                                                       BigDecimal maxPrice,
                                                       List<String> stockStatus,
                                                       String sortBy,
                                                       String sortDir,
                                                       Integer page,
                                                       Integer size) throws Exception {
        return ok(service.queryProducts(name, category, minPrice, maxPrice, stockStatus, sortBy, sortDir, page, size));
    }
}
