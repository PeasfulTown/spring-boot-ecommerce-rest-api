package xyz.peasfultown.ecommerce.order_service.dto;

import lombok.*;
import xyz.peasfultown.ecommerce.order_api.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateMessage {
    private String userId;
    private String email;
    private String phone;
    private String streetNumber;
    private String streetName;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private BigDecimal totalPrice;
    private int itemCount;
    private List<OrderItem> items;
}
