package xyz.peasfultown.ecommerce.product_service.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import xyz.peasfultown.ecommerce.product_api.model.NewProductReq;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_api.model.ProductId;
import xyz.peasfultown.ecommerce.product_api.model.ProductStockStatus;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Page<Product> queryProducts(String name,
                                String category,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                List<ProductStockStatus> stockStatus,
                                String sortBy,
                                String sortDir,
                                Integer page,
                                Integer size);

    Product createProduct(NewProductReq newProductReq);

    void deleteProductById(String id);

    Product getProductById(String id);

    List<Product> getProducts(List<@Valid ProductId> productId);
}
