package xyz.peasfultown.ecommerce.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.peasfultown.ecommerce.auth_service.entity.RefreshTokenEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    @Query("""
            select t from RefreshTokenEntity t where t.token = :refreshToken
            """)
    Optional<RefreshTokenEntity> findRefreshTokenByToken(UUID refreshToken);
    @Query("""
            select t from RefreshTokenEntity t where t.account.id = :accountId
            """)
    List<RefreshTokenEntity> findRefreshTokensByAccountId(UUID accountId);
}
