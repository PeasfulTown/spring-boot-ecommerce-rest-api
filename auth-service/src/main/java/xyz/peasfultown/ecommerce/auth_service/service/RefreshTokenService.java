package xyz.peasfultown.ecommerce.auth_service.service;

import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;

public interface RefreshTokenService {
    String createRefreshToken(AccountEntity accountEntity);
}
