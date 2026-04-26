package xyz.peasfultown.ecommerce.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class OrderConfirmationMessage {
    private String orderId;
    private String paymentId;
    private OffsetDateTime paidAt;
}
