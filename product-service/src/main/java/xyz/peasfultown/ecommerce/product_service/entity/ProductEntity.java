package xyz.peasfultown.ecommerce.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
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
    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

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

    @Column(name = "active_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActiveStatus activeStatus = ActiveStatus.INACTIVE;

    @Column(name = "stock_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus = StockStatus.OUT_OF_STOCK;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public enum ActiveStatus {
        ACTIVE, INACTIVE;

        public static ActiveStatus fromValue(String value) {
            for (ActiveStatus s : ActiveStatus.values())
                if (s.toString().equalsIgnoreCase(value)) return s;
            throw new IllegalArgumentException(String.format("Unexpected ActiveStatus value: %s", value));
        }
    }

    public enum StockStatus {
        OUT_OF_STOCK, IN_STOCK, LOW_STOCK;

        public static StockStatus fromValue(String value) {
            for (StockStatus s : StockStatus.values())
                if (s.name().equalsIgnoreCase(value)) return s;
            throw new IllegalArgumentException(String.format("Unexpected StockStatus value: %s", value));
        }
    }
}
