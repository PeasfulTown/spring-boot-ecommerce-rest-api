package xyz.peasfultown.ecommerce.user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class BadRequestException extends CustomErrorResponseException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
