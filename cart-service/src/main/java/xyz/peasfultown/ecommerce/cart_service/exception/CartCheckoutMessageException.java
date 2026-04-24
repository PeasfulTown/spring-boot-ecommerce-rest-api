package xyz.peasfultown.ecommerce.cart_service.exception;

import org.springframework.http.HttpStatus;

public class CartCheckoutMessageException extends CustomErrorResponseException {
    public CartCheckoutMessageException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
