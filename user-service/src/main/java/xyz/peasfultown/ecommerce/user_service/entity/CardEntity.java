package xyz.peasfultown.ecommerce.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardEntity {
    @Builder.Default
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id = UUID.randomUUID();

    @Column(name = "card_holder_name", nullable = false, length = 100)
    private String cardHolderName;

    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")
    private CardType cardType;

    @Column(name = "expiry_month")
    private int expiryMonth;

    @Column(name = "expiry_year")
    private int expiryYear;

    @Column(name = "token", nullable = false)
    private String token;

    @Builder.Default
    @Column(name = "is_default")
    private boolean isDefault = false;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public enum CardType {
        VISA, AMEX, MASTERCARD;

        public CardType fromValue(String value) {
            for (CardType c : CardType.values())
                if (c.name().equals(value))
                    return c;
            throw new IllegalArgumentException(String.format(
                    "Unknown card type: %s", value
            ));
        }

        public String getValue() {
            return this.name();
        }
    }
}
