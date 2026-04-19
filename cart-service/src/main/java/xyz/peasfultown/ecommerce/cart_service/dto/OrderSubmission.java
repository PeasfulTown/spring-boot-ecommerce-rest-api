package xyz.peasfultown.ecommerce.cart_service.dto;

import lombok.*;
import xyz.peasfultown.ecommerce.cart_api.model.Cart;
import xyz.peasfultown.ecommerce.cart_api.model.CartItem;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSubmission {
    private String userId;
    private String contactEmail;
    private String contactPhone;
    private String addressNumber;
    private String addressStreet;
    private String addressCity;
    private String addressState;
    private String addressCountry;
    private String addressPostalCode;
    private BigDecimal orderTotal;
    private int orderItemCount;
    private List<CartItem> items;

    public OrderSubmission(User user, Address address, Cart cart) {
        this.setUserId(user.getId());
        this.setContactEmail(user.getEmail());
        this.setContactPhone(user.getPhone());
        this.setAddressNumber(address.getNumber());
        this.setAddressStreet(address.getStreet());
        this.setAddressCity(address.getCity());
        this.setAddressState(address.getState());
        this.setAddressPostalCode(address.getPostalCode());
        this.setItems(cart.getItems());
        this.setOrderTotal(cart.getTotalPrice());
        this.setOrderItemCount(cart.getTotalItems());
    }
}
