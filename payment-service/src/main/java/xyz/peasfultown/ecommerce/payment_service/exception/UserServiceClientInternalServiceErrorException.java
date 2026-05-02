package xyz.peasfultown.ecommerce.payment_service.exception;

import org.springframework.http.HttpStatus;

public class UserServiceClientInternalServiceErrorException extends CustomErrorResponseException {
    public UserServiceClientInternalServiceErrorException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
