package xyz.peasfultown.ecommerce.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {
    @Builder.Default
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id = UUID.randomUUID();

    @Column(name = "order_id", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID orderId;

    @Column(name = "user_id", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID userId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "card_token", updatable = false)
    private String cardToken;

    @Column(name = "amount")
    private BigDecimal amount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PROCESSING;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "note")
    private String note;

    public enum PaymentStatus {
        PROCESSING, SUCCESS, FAILED;

        public String getValue() {
            return this.name();
        }

        public PaymentStatus fromValue(String value) {
            for (PaymentStatus s : PaymentStatus.values())
                if (s.name().equals(value))
                    return s;
            throw new IllegalArgumentException(String.format(
                    "Unknown payment status value of: %s", value
            ));
        }
    }
}
