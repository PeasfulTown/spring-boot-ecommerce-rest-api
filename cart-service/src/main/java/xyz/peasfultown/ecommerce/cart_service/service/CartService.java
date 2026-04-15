package xyz.peasfultown.ecommerce.cart_service.service;

import xyz.peasfultown.ecommerce.cart_api.model.AddItemReq;
import xyz.peasfultown.ecommerce.cart_api.model.Cart;
import xyz.peasfultown.ecommerce.cart_api.model.CartItem;
import xyz.peasfultown.ecommerce.cart_api.model.UpdateItemQuantityReq;

public interface CartService {
    Cart getCartByUserId(String userId);

    CartItem addItemToCart(String userId, AddItemReq req);

    void clearUserCart(String userId);

    void removeItemFromCart(String userId, String itemId);

    CartItem updateCartItemQuantity(String userId, String itemId, UpdateItemQuantityReq req);
}
