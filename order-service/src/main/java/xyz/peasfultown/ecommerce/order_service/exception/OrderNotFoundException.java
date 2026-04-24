package xyz.peasfultown.ecommerce.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public class OrderNotFoundException extends CustomErrorResponseException {
    public OrderNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
