package xyz.peasfultown.ecommerce.product_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponseException;

public class CategoryNotFoundException extends ErrorResponseException {
    public CategoryNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND);
        super.setDetail(message);
    }
}
