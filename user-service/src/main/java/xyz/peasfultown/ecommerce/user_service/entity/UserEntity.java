package xyz.peasfultown.ecommerce.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "`user`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(name = "phone", length = 10, nullable = false)
    private String phone;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Builder.Default
    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL
    )
    private List<AddressEntity> addresses = new ArrayList<>();

    public void addAddress(AddressEntity ae) {
        ae.setUser(this);
        this.addresses.add(ae);
    }

    public void addAddresses(List<AddressEntity> aes) {
        aes.forEach(ae -> ae.setUser(this));
        this.getAddresses().addAll(aes);
    }
}
