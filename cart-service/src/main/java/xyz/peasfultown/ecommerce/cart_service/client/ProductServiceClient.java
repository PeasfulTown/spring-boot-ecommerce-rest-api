package xyz.peasfultown.ecommerce.cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.peasfultown.ecommerce.cart_api.model.Product;

@FeignClient(
        name = "product-service",
        url = "${services.product-service-url}",
        path = "/api/v1/products"
)
public interface ProductServiceClient {
    @GetMapping("/{id}")
    ResponseEntity<Product> getProductById(@PathVariable("id") String productId);
}
