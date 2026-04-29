package xyz.peasfultown.ecommerce.auth_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RefreshTokenEntity;
import xyz.peasfultown.ecommerce.auth_service.exception.InvalidRefreshTokenException;
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

    public RefreshTokenEntity createRefreshToken(AccountEntity ae) {
        RefreshTokenEntity rte = RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .account(ae)
                .token(UUID.randomUUID())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .build();
        return repo.save(rte);
    }

    @Override
    public RefreshTokenEntity validateRefreshToken(String refreshToken) {
        RefreshTokenEntity refToken = repo.findRefreshTokenByToken(UUID.fromString(refreshToken))
                .orElseThrow(() -> new InvalidRefreshTokenException(String.format(
                        "Refresh token not found in database: %s", refreshToken
                )));

        if (refToken.getExpiresAt().isBefore(Instant.now()))
            throw new InvalidRefreshTokenException(String.format(
                    "Refresh token expired: %s", refreshToken
            ));

        if (refToken.isRevoked())
            throw new InvalidRefreshTokenException(String.format(
                    "Refresh token is revoked: %s", refreshToken
            ));

        return refToken;
    }
}
