package xyz.peasfultown.ecommerce.cart_service.exception;

public class EmptyCartCheckoutException extends RuntimeException {
    public EmptyCartCheckoutException(String message) {
        super(message);
    }
}
