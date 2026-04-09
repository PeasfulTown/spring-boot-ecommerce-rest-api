package xyz.peasfultown.ecommerce.product_service.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.product_api.ProductApi;
import xyz.peasfultown.ecommerce.product_api.model.PagedProductResponse;
import xyz.peasfultown.ecommerce.product_api.model.PagedResponsePage;
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
    public ResponseEntity<PagedProductResponse> queryProducts(String name,
                                                                       String category,
                                                                       BigDecimal minPrice,
                                                                       BigDecimal maxPrice,
                                                                       List<String> stockStatus,
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
}
