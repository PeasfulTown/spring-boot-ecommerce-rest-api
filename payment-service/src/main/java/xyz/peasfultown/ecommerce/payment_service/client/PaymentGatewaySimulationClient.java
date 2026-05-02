package xyz.peasfultown.ecommerce.payment_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.peasfultown.ecommerce.payment_service.dto.CardToken;
import xyz.peasfultown.ecommerce.payment_service.dto.PaymentGatewayResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
public class PaymentGatewaySimulationClient {

    public PaymentGatewayResponse simulatePaymentGateway(CardToken cardToken,
                                                         BigDecimal amount) {
        String transactionId = "sim_txn_" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 16);

        if (isExpired(cardToken.getExpiryMonth(), cardToken.getExpiryYear()))
            return PaymentGatewayResponse.failed(transactionId, "Card is expired");
        log.info("EXPIRED: {}", isExpired(cardToken.getExpiryMonth(), cardToken.getExpiryYear()));

        // simulate specific card scenarios
        // using the last digits
        switch (cardToken.getLastFourDigits()) {
            case "0002" -> {
                return PaymentGatewayResponse.failed(transactionId, "Card declined");
            }
            case "9995" -> {
                return PaymentGatewayResponse.failed(transactionId, "Insufficient funds");
            }
            case "0127" -> {
                return PaymentGatewayResponse.failed(transactionId, "Incorrect CVV");
            }
        }

        return PaymentGatewayResponse.success(transactionId);
    }

    private boolean isExpired(Integer expiryMonth, Integer expiryYear) {
        LocalDate expiry = LocalDate.of(expiryYear, expiryMonth, 1);
        return expiry.isBefore(LocalDate.now());
    }
}
