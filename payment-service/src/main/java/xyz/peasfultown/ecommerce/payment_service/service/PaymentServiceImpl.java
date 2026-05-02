package xyz.peasfultown.ecommerce.payment_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.ecommerce.payment_service.client.PaymentGatewaySimulationClient;
import xyz.peasfultown.ecommerce.payment_service.client.UserServiceClient;
import xyz.peasfultown.ecommerce.payment_service.dto.*;
import xyz.peasfultown.ecommerce.payment_service.entity.PaymentEntity;
import xyz.peasfultown.ecommerce.payment_service.exception.UserServiceClientNotFoundException;
import xyz.peasfultown.ecommerce.payment_service.messaging.MessagePublisher;
import xyz.peasfultown.ecommerce.payment_service.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserServiceClient userClient;
    private final PaymentGatewaySimulationClient paymentClient;
    private final MessagePublisher messagePublisher;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, UserServiceClient userClient, PaymentGatewaySimulationClient paymentClient, MessagePublisher messagePublisher) {
        this.paymentRepository = paymentRepository;
        this.userClient = userClient;
        this.paymentClient = paymentClient;
        this.messagePublisher = messagePublisher;
    }

    @Override
    public void validatePayment(PaymentConfirmationMessage message) {
        CardToken cardToken = null;

        PaymentEntity pe = PaymentEntity.builder()
                .orderId(UUID.fromString(message.getOrderId()))
                .userId(UUID.fromString(message.getUserId()))
                .amount(message.getAmount())
                    .build();

        try {
            cardToken = userClient.getCardToken(message.getCardId());
        } catch (UserServiceClientNotFoundException e) {
            pe.setPaymentStatus(PaymentEntity.PaymentStatus.FAILED);
            pe.setNote("Card token not found");
        }

        if (cardToken != null) {
            pe.setPaymentStatus(PaymentEntity.PaymentStatus.PROCESSING);
            pe.setCardToken(cardToken.getToken());
        }

        paymentRepository.save(pe);

        PaymentGatewayResponse res = cardToken == null
            ? null
            : paymentClient.simulatePaymentGateway(cardToken, message.getAmount());

        if (res != null) {
            if (res.isSuccess()) {
                pe.setPaymentStatus(PaymentEntity.PaymentStatus.SUCCESS);
            } else {
                pe.setPaymentStatus(PaymentEntity.PaymentStatus.FAILED);
                pe.setNote(res.getReason());
            }

            pe.setTransactionId(res.getTransactionId());
            paymentRepository.save(pe);

            if (res.isSuccess()) {
                OrderConfirmationMessage ocm = OrderConfirmationMessage.builder()
                        .orderId(message.getOrderId())
                        .paymentId(pe.getId().toString())
                        .paidAt(pe.getCreatedAt().atOffset(ZoneOffset.UTC))
                        .build();
                messagePublisher.sendOrderConfirmationMessage(ocm);
            }
        }

        if (res == null || !res.isSuccess()) {
            messagePublisher.sendOrderCancellationMessage(new OrderCancellationMessage(
                    message.getOrderId(),
                    pe.getId().toString())
            );
        }
    }
}
