package xyz.peasfultown.ecommerce.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "category")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryEntity {
    @Builder.Default
    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id = UUID.randomUUID();

    @Column(name = "name", unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 100)
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<ProductEntity> products;
}
