package xyz.peasfultown.ecommerce.order_service.exception;

public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException(String message) {
        super(message);
    }
}
