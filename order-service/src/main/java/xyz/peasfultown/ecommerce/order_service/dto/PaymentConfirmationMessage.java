package xyz.peasfultown.ecommerce.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentConfirmationMessage {
    private String orderId;
    private String cardId;
}
