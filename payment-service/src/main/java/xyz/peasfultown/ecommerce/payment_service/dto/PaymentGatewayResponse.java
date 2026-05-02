package xyz.peasfultown.ecommerce.payment_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentGatewayResponse {
    private final String transactionId;
    private final String reason;
    private final boolean success;

    public static PaymentGatewayResponse success(String transactionId) {
        return PaymentGatewayResponse.builder()
                .success(true)
                .transactionId(transactionId)
                .build();
    }

    public static PaymentGatewayResponse failed(String transactionId, String reason) {
        return PaymentGatewayResponse.builder()
                .success(false)
                .transactionId(transactionId)
                .reason(reason)
                .build();
    }
}
