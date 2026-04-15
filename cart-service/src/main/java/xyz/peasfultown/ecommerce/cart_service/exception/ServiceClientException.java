package xyz.peasfultown.ecommerce.cart_service.exception;

import org.springframework.http.HttpStatus;

public class ServiceClientException extends RuntimeException {
    private final HttpStatus status;
    private final String reason;

    public ServiceClientException(HttpStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public ServiceClientException(HttpStatus status, String reason, String message){
        super(message);
        this.status = status;
        this.reason = reason;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public String getReason() {
        return this.reason;
    }
}
