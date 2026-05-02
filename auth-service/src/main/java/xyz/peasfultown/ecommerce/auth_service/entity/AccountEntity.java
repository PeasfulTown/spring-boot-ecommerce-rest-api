package xyz.peasfultown.ecommerce.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "`account`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {
    @Builder.Default
    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id = UUID.randomUUID();

    @Column(name = "email", length = 50, nullable = false, updatable = false)
    private String email;

    @Column(name = "password", length = 50, nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleEnum role = RoleEnum.CUSTOMER;

    @OneToMany(
        mappedBy = "account",
        cascade = CascadeType.ALL
    )
    private List<RefreshTokenEntity> refreshTokens = new ArrayList<>();

    public void addRefreshToken(RefreshTokenEntity rte) {
        rte.setAccount(this);
        this.refreshTokens.add(rte);
    }
}
