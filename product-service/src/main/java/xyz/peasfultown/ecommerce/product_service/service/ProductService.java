package xyz.peasfultown.ecommerce.product_service.service;

import org.springframework.data.domain.Page;
import xyz.peasfultown.ecommerce.product_api.model.*;
import xyz.peasfultown.ecommerce.product_service.dto.ProductStockUpdateMessage;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Page<Product> queryProducts(String name,
                                String category,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                List<StockStatus> stockStatus,
                                String sortBy,
                                String sortDir,
                                Integer page,
                                Integer size);

    Product createProduct(ProductCreateRequest newProductReq);

    void deleteProductById(String id);

    Product getProductById(String id);

    List<Product> getProducts(BatchProductIdRequest request);

    Product updateProductById(String productId, ProductUpdateRequest productUpdateRequest);

    void updateProductStock(ProductStockUpdateMessage dto);
}
