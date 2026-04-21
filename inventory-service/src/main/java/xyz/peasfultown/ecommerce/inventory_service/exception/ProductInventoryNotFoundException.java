package xyz.peasfultown.ecommerce.inventory_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;

public class ProductInventoryNotFoundException extends ErrorResponseException {
    private String message;
    public ProductInventoryNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND);
        super.setDetail(message);
    }


}
