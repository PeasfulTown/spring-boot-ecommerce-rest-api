package xyz.peasfultown.ecommerce.cart_service.exception;

import org.springframework.http.HttpStatus;

public class ProductServiceClientNotFoundException extends CustomErrorResponseException {
    public ProductServiceClientNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
