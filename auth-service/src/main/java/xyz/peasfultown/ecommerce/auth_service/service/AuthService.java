package xyz.peasfultown.ecommerce.auth_service.service;

import jakarta.validation.Valid;
import xyz.peasfultown.ecommerce.auth_api.model.Authentication;
import xyz.peasfultown.ecommerce.auth_api.model.LoginReq;
import xyz.peasfultown.ecommerce.auth_api.model.NewAccountReq;
import xyz.peasfultown.ecommerce.auth_api.model.RefreshToken;

public interface AuthService {
    Authentication createNewAccount(@Valid NewAccountReq newAccountReq);

    Authentication getToken(@Valid LoginReq loginReq);

    Authentication renewAccessToken(RefreshToken refreshToken);

    void logout(Authentication authentication);
}
