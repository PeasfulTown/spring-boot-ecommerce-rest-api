package xyz.peasfultown.ecommerce.cart_service.exception;

import org.springframework.http.HttpStatus;

public class ProductServiceClientException extends CustomErrorResponseException {
    public ProductServiceClientException(HttpStatus httpStatus, String body) {
        super(httpStatus, body);
    }
}
