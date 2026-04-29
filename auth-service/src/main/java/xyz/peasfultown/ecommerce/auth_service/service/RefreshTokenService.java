package xyz.peasfultown.ecommerce.auth_service.service;

import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RefreshTokenEntity;

public interface RefreshTokenService {
    RefreshTokenEntity createRefreshToken(AccountEntity accountEntity);

    RefreshTokenEntity validateRefreshToken(String refreshToken);
}
