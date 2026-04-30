package xyz.peasfultown.ecommerce.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class UserServiceNotFoundException extends CustomErrorResponseException{
    public UserServiceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
