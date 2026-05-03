package xyz.peasfultown.ecommerce.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEntity {
    @Builder.Default
    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id = UUID.randomUUID();

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @ElementCollection
    @CollectionTable(
            name = "product_image_urls",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "image_url")
    private List<String> imageUrls;

    @Builder.Default
    @Column(name = "active_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActiveStatus activeStatus = ActiveStatus.INACTIVE;

    @Builder.Default
    @Column(name = "stock", nullable = false)
    private int stock = 0;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public enum ActiveStatus {
        ACTIVE, INACTIVE;

        public static ActiveStatus fromValue(String value) {
            for (ActiveStatus s : ActiveStatus.values())
                if (value.equalsIgnoreCase(s.name())) return s;
            throw new IllegalArgumentException(String.format("Unexpected ActiveStatus value: %s", value));
        }
    }

}
