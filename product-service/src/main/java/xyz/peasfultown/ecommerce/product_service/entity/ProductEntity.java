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

    @Setter(AccessLevel.NONE)
    @Column(name = "stock_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public StockStatus calculateStockStatus() {
        if (stock == 0)
            return StockStatus.OUT_OF_STOCK;
        else if (stock <= 20)
            return StockStatus.LOW_STOCK;

        return StockStatus.IN_STOCK;
    }

    public void setStockStatus() {
        this.stockStatus = calculateStockStatus();
    }

    public enum ActiveStatus {
        ACTIVE, INACTIVE;

        public static ActiveStatus fromValue(String value) {
            for (ActiveStatus s : ActiveStatus.values())
                if (value.equalsIgnoreCase(s.name())) return s;
            throw new IllegalArgumentException(String.format("Unexpected ActiveStatus value: %s", value));
        }
    }

    public enum StockStatus {
        OUT_OF_STOCK, IN_STOCK, LOW_STOCK;

        public static StockStatus fromValue(String value) {
            for (StockStatus s : StockStatus.values())
                if (value.equalsIgnoreCase(s.name())) return s;
            throw new IllegalArgumentException(String.format("Unexpected StockStatus value: %s", value));
        }
    }
}
