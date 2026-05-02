package xyz.peasfultown.ecommerce.payment_service.exception;

import org.springframework.http.HttpStatus;

public class UserServiceClientNotFoundException extends CustomErrorResponseException {
    public UserServiceClientNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
