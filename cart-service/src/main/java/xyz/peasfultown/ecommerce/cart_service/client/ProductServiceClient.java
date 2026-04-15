package xyz.peasfultown.ecommerce.cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import xyz.peasfultown.ecommerce.cart_api.model.Product;
import xyz.peasfultown.ecommerce.cart_api.model.ProductId;
import xyz.peasfultown.ecommerce.cart_service.config.OpenFeignConfig;
import xyz.peasfultown.ecommerce.cart_service.exception.CustomProductServiceErrorDecoder;

import java.util.List;

@FeignClient(
        name = "product-service",
        url = "${services.product-service-url}",
        path = "/api/v1/products",
        configuration = { OpenFeignConfig.class, CustomProductServiceErrorDecoder.class }
)
public interface ProductServiceClient {
    @GetMapping("/{id}")
    ResponseEntity<Product> getProductById(@PathVariable("id") String productId);

    @PostMapping("/batch")
    ResponseEntity<List<Product>> getProductsByIds(List<ProductId> productIds);
}
