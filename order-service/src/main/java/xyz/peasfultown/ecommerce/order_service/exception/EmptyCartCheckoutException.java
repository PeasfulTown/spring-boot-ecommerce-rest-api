package xyz.peasfultown.ecommerce.order_service.exception;

public class EmptyCartCheckoutException extends RuntimeException {
    public EmptyCartCheckoutException(String message) {
        super(message);
    }
}
