package xyz.peasfultown.ecommerce.order_service.exception;

public class EmptyCardCheckoutException extends RuntimeException {
    public EmptyCardCheckoutException(String message) {
        super(message);
    }
}
