package xyz.peasfultown.ecommerce.order_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "street", nullable = false)
    private String street;

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PROCESSING;

    @Builder.Default
    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
    private List<OrderItemEntity> items = new ArrayList<>();

    public enum OrderStatus {
        PROCESSING, SHIPPED, OUT_FOR_DELIVERY, COMPLETED, CANCELLED;
    }
}
