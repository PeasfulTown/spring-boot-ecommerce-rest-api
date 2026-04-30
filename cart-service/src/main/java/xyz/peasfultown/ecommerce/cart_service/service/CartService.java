package xyz.peasfultown.ecommerce.cart_service.service;

import xyz.peasfultown.ecommerce.cart_api.model.*;

public interface CartService {
    Cart getCartByUserId(String userId);

    CartItem addItemToCart(String userId, ItemCreateRequest req);

    void clearCart(String userId);

    void removeItemFromCart(String userId, String itemId);

    CartItem updateCartItemQuantity(String userId, String itemId, ItemQuantityUpdateRequest req);

    void checkoutCart(String userId, CartCheckoutRequest cartCheckoutReq);
}
