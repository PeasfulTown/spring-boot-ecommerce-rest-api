package xyz.peasfultown.ecommerce.cart_service.exception;

import org.springframework.http.HttpStatus;

public class CartNotFoundException extends CustomErrorResponseException {
    public CartNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
