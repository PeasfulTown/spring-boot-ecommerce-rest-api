package xyz.peasfultown.ecommerce.user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AccessForbiddenException extends CustomErrorResponseException {
    public AccessForbiddenException() {
        this("Access forbidden");
    }

    public AccessForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
