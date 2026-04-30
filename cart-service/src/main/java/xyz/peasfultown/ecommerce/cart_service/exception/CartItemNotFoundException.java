package xyz.peasfultown.ecommerce.cart_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class CartItemNotFoundException extends CustomErrorResponseException {
    public CartItemNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
