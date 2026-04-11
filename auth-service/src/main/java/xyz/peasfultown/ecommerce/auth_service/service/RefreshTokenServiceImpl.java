package xyz.peasfultown.ecommerce.auth_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RefreshTokenEntity;
import xyz.peasfultown.ecommerce.auth_service.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Value("${jwt.expiry.refreshToken}")
    private long refreshTokenExpiration;

    private final RefreshTokenRepository repo;

    public RefreshTokenServiceImpl(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    public String createRefreshToken(AccountEntity ae) {
        RefreshTokenEntity rte = RefreshTokenEntity.builder()
                .account(ae)
                .token(UUID.randomUUID())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        repo.save(rte);
        return rte.getToken().toString();
    }
}
