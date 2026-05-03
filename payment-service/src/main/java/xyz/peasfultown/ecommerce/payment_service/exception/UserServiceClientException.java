package xyz.peasfultown.ecommerce.payment_service.exception;

import org.springframework.http.HttpStatus;

public class UserServiceClientException extends CustomErrorResponseException {
    public UserServiceClientException(HttpStatus status, String message) {
        super(status, message);
    }
}
