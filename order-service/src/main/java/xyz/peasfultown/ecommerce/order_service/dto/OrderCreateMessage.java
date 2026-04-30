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
    private String cardId;
    private String addressId;
    private BigDecimal totalPrice;
    private int itemCount;
    private List<OrderItem> items;
}
