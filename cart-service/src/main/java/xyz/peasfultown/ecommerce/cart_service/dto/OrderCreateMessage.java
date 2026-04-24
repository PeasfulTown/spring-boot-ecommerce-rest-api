package xyz.peasfultown.ecommerce.cart_service.dto;

import lombok.*;
import xyz.peasfultown.ecommerce.cart_api.model.Cart;
import xyz.peasfultown.ecommerce.cart_api.model.CartItem;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateMessage {
    private String userId;
    private String cardId;
    private String addressId;
    private BigDecimal totalPrice;
    private int itemCount;
    private List<CartItem> items;
}
