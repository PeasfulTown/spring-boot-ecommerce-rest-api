package xyz.peasfultown.ecommerce.user_service.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("Not authorized to perform this action");
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
