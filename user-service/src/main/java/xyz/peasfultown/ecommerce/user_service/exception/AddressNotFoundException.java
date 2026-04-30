package xyz.peasfultown.ecommerce.user_service.exception;

import org.springframework.http.HttpStatus;

public class AddressNotFoundException extends CustomErrorResponseException {
    public AddressNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
