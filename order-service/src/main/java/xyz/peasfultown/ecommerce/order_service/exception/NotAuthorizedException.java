package xyz.peasfultown.ecommerce.order_service.exception;

import org.springframework.http.HttpStatus;

public class NotAuthorizedException extends CustomErrorResponseException{
    public NotAuthorizedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
