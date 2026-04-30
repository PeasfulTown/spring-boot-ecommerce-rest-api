package xyz.peasfultown.ecommerce.cart_service.exception;

import feign.Response;
import org.springframework.http.HttpStatus;

public class ServiceClientException extends CustomErrorResponseException {
    public ServiceClientException(HttpStatus status, Response response) {
        super(status, response.reason());
    }
}
