package xyz.peasfultown.ecommerce.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class OrderInformationNotFoundException extends CustomErrorResponseException {
    public OrderInformationNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
