package xyz.peasfultown.ecommerce.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PaymentConfirmationMessage {
    private String userId;
    private String orderId;
    private String cardId;
    private BigDecimal amount;
}
