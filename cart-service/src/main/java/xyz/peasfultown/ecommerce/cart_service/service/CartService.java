package xyz.peasfultown.ecommerce.cart_service.service;

import xyz.peasfultown.ecommerce.cart_api.model.AddItemReq;
import xyz.peasfultown.ecommerce.cart_api.model.Cart;
import xyz.peasfultown.ecommerce.cart_api.model.CartItem;

public interface CartService {
    Cart getCartByUserId(String userId);

    CartItem addItemToCart(String userId, AddItemReq req);
}
