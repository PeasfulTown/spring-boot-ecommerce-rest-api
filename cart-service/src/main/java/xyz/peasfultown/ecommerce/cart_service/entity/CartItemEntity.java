package xyz.peasfultown.ecommerce.cart_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemEntity {
    @Builder.Default
    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Builder.Default
    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private CartEntity cart;
}
