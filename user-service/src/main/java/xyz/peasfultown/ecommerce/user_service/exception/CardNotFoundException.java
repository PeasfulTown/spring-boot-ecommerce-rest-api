package xyz.peasfultown.ecommerce.user_service.exception;

import org.springframework.http.HttpStatus;

public class CardNotFoundException extends CustomErrorResponseException {
    public CardNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
