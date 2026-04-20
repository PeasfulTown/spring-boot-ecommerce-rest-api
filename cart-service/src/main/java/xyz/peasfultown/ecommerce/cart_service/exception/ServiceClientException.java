package xyz.peasfultown.ecommerce.cart_service.exception;

import feign.Response;
import org.springframework.http.HttpStatus;

public class ServiceClientException extends RuntimeException {
    private final HttpStatus status;
    private final Response response;

    public ServiceClientException(HttpStatus status, Response response) {
        this.status = status;
        this.response = response;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public Response getResponse() {
        return response;
    }
}
