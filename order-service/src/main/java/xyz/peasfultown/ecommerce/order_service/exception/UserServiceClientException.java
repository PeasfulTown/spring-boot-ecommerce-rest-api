package xyz.peasfultown.ecommerce.order_service.exception;

import org.springframework.http.HttpStatus;

public class UserServiceClientException extends CustomErrorResponseException {
    public UserServiceClientException(HttpStatus httpStatus, String body) {
        super(httpStatus, body);
    }
}
