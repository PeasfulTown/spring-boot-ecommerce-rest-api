package xyz.peasfultown.ecommerce.order_service.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "`order`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {
    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "fullname", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "street_number", nullable = false)
    private String streetNumber;

    @Column(name = "street_name", nullable = false)
    private String streetName;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "item_count", nullable = false)
    private int itemCount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PROCESSING;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> items = new ArrayList<>();

    @Column(name = "payment_id")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID paymentId;

    @Column(name = "paid_at")
    private Instant paidAt;

    public void addItem(OrderItemEntity oie) {
        oie.setOrder(this);
        this.getItems().add(oie);
    }

    public void addItems(List<OrderItemEntity> oie) {
        oie.forEach(i -> i.setOrder(this));
        this.getItems().addAll(oie);
    }

    public enum OrderStatus {
        PROCESSING("PROCESSING"),
        CONFIRMED("CONFIRMED"),
        SHIPPED("SHIPPED"),
        OUT_FOR_DELIVERY("OUT_FOR_DELIVERY"),
        COMPLETED("COMPLETED"),
        CANCELLED("CANCELLED");

        private final String value;
        OrderStatus(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static OrderStatus fromValue(String in) {
            for (OrderStatus s : OrderStatus.values())
                if (in.equalsIgnoreCase(s.getValue()))
                    return s;

            throw new IllegalArgumentException(String.format(
                    "Unknown OrderStatus value: %s", in
            ));
        }
    }
}
