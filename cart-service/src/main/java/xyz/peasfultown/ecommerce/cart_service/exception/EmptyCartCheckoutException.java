package xyz.peasfultown.ecommerce.cart_service.exception;

import org.springframework.http.HttpStatus;

public class EmptyCartCheckoutException extends CustomErrorResponseException {
    public EmptyCartCheckoutException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
