package xyz.peasfultown.ecommerce.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class OrderConfirmationMessage {
    private String orderId;
    private String paymentId;
    private OffsetDateTime paidAt;
}
