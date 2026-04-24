package xyz.peasfultown.ecommerce.order_service.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponseException;

// doing this to get rid of the useless "type" field of the default ProblemDetail response...
public class CustomErrorResponseException extends ErrorResponseException {
    public CustomErrorResponseException(HttpStatusCode status, String message) {
        super(status);
        super.setDetail(message);
        super.setType(null);
    }
}
