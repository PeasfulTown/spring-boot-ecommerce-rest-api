package xyz.peasfultown.ecommerce.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.peasfultown.ecommerce.payment_service.entity.PaymentEntity;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
}
