package xyz.peasfultown.ecommerce.cart_service.service;

import xyz.peasfultown.ecommerce.cart_api.model.Cart;

public interface CartService {
    Cart getCartByUserId(String userId);
}
