package xyz.peasfultown.ecommerce.auth_service.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponseException;

public class CustomErrorResponseException extends ErrorResponseException {
    public CustomErrorResponseException(HttpStatusCode status, String message) {
        super(status);
        super.setDetail(message);
        super.setType(null);
    }
}
