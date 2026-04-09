package xyz.peasfultown.ecommerce.product_service.service;

import org.springframework.data.domain.Page;
import xyz.peasfultown.ecommerce.product_api.model.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Page<Product> queryProducts(String name,
                                String category,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                List<String> stockStatus,
                                String sortBy,
                                String sortDir,
                                Integer page,
                                Integer size);
}
