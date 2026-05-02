package xyz.peasfultown.ecommerce.payment_service.service;

import xyz.peasfultown.ecommerce.payment_service.dto.PaymentConfirmationMessage;

public interface PaymentService {
    void validatePayment(PaymentConfirmationMessage message);
}
