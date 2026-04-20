package xyz.peasfultown.ecommerce.cart_service.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super(String.format("Product not foud by ID: %s", productId));
    }
}
