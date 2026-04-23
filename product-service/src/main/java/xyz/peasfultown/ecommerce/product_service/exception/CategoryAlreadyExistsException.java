package xyz.peasfultown.ecommerce.product_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponseException;

public class CategoryAlreadyExistsException extends ErrorResponseException {
    public CategoryAlreadyExistsException(String message) {
        super(HttpStatus.BAD_REQUEST);
        super.setDetail(message);
    }
}
