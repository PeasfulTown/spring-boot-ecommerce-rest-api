package xyz.peasfultown.ecommerce.cart_service.exception;

import org.springframework.http.HttpStatus;

public class ProductOutOfStockException extends CustomErrorResponseException {
    public ProductOutOfStockException(String id) {
        super(HttpStatus.BAD_REQUEST, String.format("Product out of stock ID: %s", id));
    }
}
