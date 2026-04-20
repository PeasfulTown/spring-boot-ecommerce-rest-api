package xyz.peasfultown.ecommerce.cart_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.cart_api.CartApi;
import xyz.peasfultown.ecommerce.cart_api.model.*;
import xyz.peasfultown.ecommerce.cart_service.service.CartService;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class CartController implements CartApi {
    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<Cart> getMyCart(String userId) throws Exception {
        return ok(service.getCartByUserId(userId));
    }

    @Override
    public ResponseEntity<CartItem> addItemToCart(String userId, AddItemReq req) throws Exception {
        return ok(service.addItemToCart(userId, req));
    }

    @Override
    public ResponseEntity<Void> clearMyCart(String userId) throws Exception {
        service.clearUserCart(userId);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> deleteItemFromCart(String userId, String itemId) throws Exception {
        service.removeItemFromCart(userId, itemId);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<CartItem> updateItemQuantity(String userId, String itemId, UpdateItemQuantityReq req) throws Exception {
        return ok(service.updateCartItemQuantity(userId, itemId, req));
    }

    @Override
    public ResponseEntity<Void> submitOrder(String userId, CartCheckoutReq cartCheckoutReq) throws Exception {
        service.checkoutCart(userId, cartCheckoutReq);
        return status(HttpStatus.ACCEPTED).build();
    }
}
