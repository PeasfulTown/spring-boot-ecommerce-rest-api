package xyz.peasfultown.ecommerce.auth_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.auth_api.AuthApi;
import xyz.peasfultown.ecommerce.auth_api.model.Authentication;
import xyz.peasfultown.ecommerce.auth_api.model.LoginReq;
import xyz.peasfultown.ecommerce.auth_api.model.NewAccountReq;
import xyz.peasfultown.ecommerce.auth_api.model.RefreshToken;
import xyz.peasfultown.ecommerce.auth_service.service.AuthService;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class AuthController implements AuthApi {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<Authentication> register(@Valid NewAccountReq newAccountReq) throws Exception {
        return status(HttpStatus.CREATED).body(service.createNewAccount(newAccountReq));
    }

    @Override
    public ResponseEntity<Authentication> login(@Valid LoginReq loginReq) throws Exception {
        return ok(service.getToken(loginReq));
    }

    @Override
    public ResponseEntity<Authentication> renewAccessToken(RefreshToken refreshToken) throws Exception {
        return ok(service.renewAccessToken(refreshToken));
    }

    @Override
    public ResponseEntity<Void> logout(Authentication authentication) throws Exception {
        service.logout(authentication);
        return status(HttpStatus.NO_CONTENT).build();
    }
}
