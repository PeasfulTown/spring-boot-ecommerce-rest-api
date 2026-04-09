package xyz.peasfultown.ecommerce.product_service.service;

import xyz.peasfultown.ecommerce.product_api.model.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    List<Product> queryProducts(String name,
                                String category,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                List<String> stockStatus,
                                String sortBy,
                                String sortDir,
                                Integer page,
                                Integer size);
}
