package xyz.peasfultown.ecommerce.cart_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.cart_api.CartApi;
import xyz.peasfultown.ecommerce.cart_api.model.Cart;
import xyz.peasfultown.ecommerce.cart_service.service.CartService;

import static org.springframework.http.ResponseEntity.ok;

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
}
