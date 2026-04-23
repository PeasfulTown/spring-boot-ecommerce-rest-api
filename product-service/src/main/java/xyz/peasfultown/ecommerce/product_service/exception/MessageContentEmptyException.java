package xyz.peasfultown.ecommerce.product_service.exception;

public class MessageContentEmptyException extends RuntimeException {
    public MessageContentEmptyException(String message) {
        super(message);
    }
}
