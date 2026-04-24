package xyz.peasfultown.ecommerce.cart_service.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends CustomErrorResponseException {
    public ProductNotFoundException(String productId) {
        super(HttpStatus.NOT_FOUND, String.format("Product not found by ID: %s", productId));
    }
}
