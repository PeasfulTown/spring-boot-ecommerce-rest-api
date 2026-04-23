package xyz.peasfultown.ecommerce.product_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public class ProductNotFoundException extends ErrorResponseException {
    public ProductNotFoundException(String productId) {
        super(HttpStatus.NOT_FOUND);
        super.setDetail("Product not found by ID: " + productId);
    }
}
