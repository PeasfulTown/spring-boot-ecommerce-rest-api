package xyz.peasfultown.ecommerce.user_service.exception;

import org.springframework.http.HttpStatus;

public class CardExpiredException extends CustomErrorResponseException {
    public CardExpiredException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
