package xyz.peasfultown.ecommerce.auth_service.exception;

public class InvalidAccountCredentialsException extends RuntimeException {
    public InvalidAccountCredentialsException(String message) {
        super(message);
    }
}
