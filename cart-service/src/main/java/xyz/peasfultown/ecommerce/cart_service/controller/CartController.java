package xyz.peasfultown.ecommerce.cart_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public CartController(CartService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<Cart> getCart(String userId) throws Exception {
        return ok(service.getCartByUserId(userId));
    }

    @Override
    public ResponseEntity<CartItem> createCartItem(String userId, ItemCreateRequest req) throws Exception {
        return ok(service.addItemToCart(userId, req));
    }

    @Override
    public ResponseEntity<Void> clearCart(String userId) throws Exception {
        service.clearCart(userId);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> removeCartItem(String userId, String itemId) throws Exception {
        service.removeItemFromCart(userId, itemId);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<CartItem> updateItemQuantity(String userId, String itemId, ItemQuantityUpdateRequest req) throws Exception {
        return ok(service.updateCartItemQuantity(userId, itemId, req));
    }

    @Override
    public ResponseEntity<Void> checkout(String userId, CartCheckoutRequest cartCheckoutReq) throws Exception {
        service.checkoutCart(userId, cartCheckoutReq);
        return status(HttpStatus.ACCEPTED).build();
    }
}
