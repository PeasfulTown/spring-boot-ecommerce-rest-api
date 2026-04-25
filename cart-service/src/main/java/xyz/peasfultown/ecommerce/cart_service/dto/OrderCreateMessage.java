package xyz.peasfultown.ecommerce.cart_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @NotEmpty
    private String userId;
    @NotEmpty
    private String cardId;
    @NotEmpty
    private String addressId;
    @NotNull
    private BigDecimal totalPrice;
    @Positive
    private int itemCount;
    @NotEmpty
    private List<CartItem> items;
}
