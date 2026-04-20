package xyz.peasfultown.ecommerce.cart_service.service;

import xyz.peasfultown.ecommerce.cart_api.model.*;

public interface CartService {
    Cart getCartByUserId(String userId);

    CartItem addItemToCart(String userId, AddItemReq req);

    void clearUserCart(String userId);

    void removeItemFromCart(String userId, String itemId);

    CartItem updateCartItemQuantity(String userId, String itemId, UpdateItemQuantityReq req);

    void checkoutCart(String userId, CartCheckoutReq cartCheckoutReq);
}
