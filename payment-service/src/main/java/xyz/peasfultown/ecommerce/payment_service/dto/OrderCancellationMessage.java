package xyz.peasfultown.ecommerce.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderCancellationMessage {
    private String orderId;
    private String paymentId;
}
