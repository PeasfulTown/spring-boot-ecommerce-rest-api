package xyz.peasfultown.ecommerce.auth_service.exception;

import org.springframework.http.HttpStatus;

public class UserServiceClientException extends CustomErrorResponseException {
    public UserServiceClientException(HttpStatus httpStatus, String responsebody) {
        super(httpStatus, responsebody);
    }
}
