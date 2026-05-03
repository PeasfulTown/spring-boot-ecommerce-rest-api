package xyz.peasfultown.ecommerce.auth_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.auth_api.model.Authentication;
import xyz.peasfultown.ecommerce.auth_api.model.LoginReq;
import xyz.peasfultown.ecommerce.auth_api.model.NewAccountReq;
import xyz.peasfultown.ecommerce.auth_api.model.RefreshToken;
import xyz.peasfultown.ecommerce.auth_service.auth.JwtUtil;
import xyz.peasfultown.ecommerce.auth_service.client.UserServiceClient;
import xyz.peasfultown.ecommerce.auth_service.dto.UserCreateRequest;
import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RefreshTokenEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RoleEnum;
import xyz.peasfultown.ecommerce.auth_service.exception.AccountAlreadyExistsException;
import xyz.peasfultown.ecommerce.auth_service.exception.InvalidAccountCredentialsException;
import xyz.peasfultown.ecommerce.auth_service.exception.InvalidRefreshTokenException;
import xyz.peasfultown.ecommerce.auth_service.repository.AuthRepository;
import xyz.peasfultown.ecommerce.auth_service.repository.RefreshTokenRepository;

import java.util.UUID;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private final AuthRepository repo;
    private final RefreshTokenRepository refRepo;
    private final RefreshTokenService refService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserServiceClient userClient;


    public AuthServiceImpl(AuthRepository repo, RefreshTokenRepository refRepo, RefreshTokenRepository refRepo1, RefreshTokenService refService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserServiceClient userClient) {
        this.repo = repo;
        this.refRepo = refRepo1;
        this.refService = refService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userClient = userClient;
    }

    @Override
    public Authentication createNewAccount(NewAccountReq newAccountReq) {
        repo.findAccountByEmail(newAccountReq.getEmail())
                .ifPresent(p -> {
                    throw new AccountAlreadyExistsException(String.format(
                            "Account already exists by email: %s", newAccountReq.getEmail()
                    ));
                });

        AccountEntity ae = AccountEntity.builder()
                .email(newAccountReq.getEmail())
                .password(passwordEncoder.encode(newAccountReq.getPassword()))
                .role(RoleEnum.CUSTOMER)
                .build();

        UserCreateRequest req = UserCreateRequest.builder()
                .id(ae.getId().toString())
                .email(ae.getEmail())
                .firstName(newAccountReq.getFirstName())
                .lastName(newAccountReq.getLastName())
                .phone(newAccountReq.getPhone())
                .build();

        userClient.createUser(req);

        ae = repo.save(ae);

        return buildAuthenticationObject(ae);
    }

    @Override
    public Authentication getToken(LoginReq loginReq) {
        AccountEntity ae = repo.findAccountByEmail(loginReq.getEmail())
                .orElseThrow(() -> new InvalidAccountCredentialsException(String.format(
                        "Account not found by email: %s", loginReq.getEmail()
                )));

        if (!passwordEncoder.matches(loginReq.getPassword(), ae.getPassword()))
            throw new InvalidAccountCredentialsException(String.format(
                    "Incorrect password for account email: %s",
                    loginReq.getEmail()
            ));

        return buildAuthenticationObject(ae);
    }

    @Override
    public Authentication renewAccessToken(RefreshToken refreshToken) {
        RefreshTokenEntity rte = refService.validateRefreshToken(refreshToken.getToken());
        rte.setRevoked(true);
        refRepo.save(rte);
        return buildAuthenticationObject(rte.getAccount());
    }

    private Authentication buildAuthenticationObject(AccountEntity ae) {
        return Authentication.builder()
                .accessToken(jwtUtil.generateAccessToken(ae))
                .refreshToken(refService.createRefreshToken(ae).getToken().toString())
                .build();

    }

    @Override
    public void logout(Authentication authentication) {
        RefreshTokenEntity rte = refRepo.findRefreshTokenByToken(UUID.fromString(authentication.getRefreshToken()))
                .orElseThrow(() -> new InvalidRefreshTokenException(String.format(
                    "Invalid refresh token: not registered in database"
                )));

        rte.setRevoked(true);

        refRepo.save(rte);
    }
}
