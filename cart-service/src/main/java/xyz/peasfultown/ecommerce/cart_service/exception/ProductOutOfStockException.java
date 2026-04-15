package xyz.peasfultown.ecommerce.cart_service.exception;

public class ProductOutOfStockException extends RuntimeException {
    public ProductOutOfStockException(String id) {
        super(String.format("Product out of stock ID: %s", id));
    }
}
