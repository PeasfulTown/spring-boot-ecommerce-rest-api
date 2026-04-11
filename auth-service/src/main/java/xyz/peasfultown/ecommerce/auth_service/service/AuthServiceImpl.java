package xyz.peasfultown.ecommerce.auth_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.auth_api.model.Authentication;
import xyz.peasfultown.ecommerce.auth_api.model.LoginReq;
import xyz.peasfultown.ecommerce.auth_api.model.NewAccountReq;
import xyz.peasfultown.ecommerce.auth_api.model.RefreshToken;
import xyz.peasfultown.ecommerce.auth_service.auth.JwtUtil;
import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RefreshTokenEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RoleEnum;
import xyz.peasfultown.ecommerce.auth_service.exception.AccountAlreadyExistsException;
import xyz.peasfultown.ecommerce.auth_service.exception.InvalidAccountCredentialsException;
import xyz.peasfultown.ecommerce.auth_service.exception.InvalidRefreshTokenException;
import xyz.peasfultown.ecommerce.auth_service.mapper.AccountMapper;
import xyz.peasfultown.ecommerce.auth_service.repository.AuthRepository;
import xyz.peasfultown.ecommerce.auth_service.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthRepository repo;
    private final RefreshTokenRepository refRepo;
    private final RefreshTokenService refService;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper mapper;
    private final JwtUtil jwtUtil;


    public AuthServiceImpl(AuthRepository repo, RefreshTokenRepository refRepo, RefreshTokenRepository refRepo1, RefreshTokenService refService, PasswordEncoder passwordEncoder, AccountMapper mapper, JwtUtil jwtUtil) {
        this.repo = repo;
        this.refRepo = refRepo1;
        this.refService = refService;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.jwtUtil = jwtUtil;
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
                .role(RoleEnum.USER)
                .build();

        ae = repo.save(ae);

        return new Authentication()
                .accessToken(jwtUtil.generateAccessToken(ae))
                .refreshToken(refService.createRefreshToken(ae));
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

        return new Authentication()
                .accessToken(jwtUtil.generateAccessToken(ae))
                .refreshToken(refService.createRefreshToken(ae));
    }

    @Override
    public Authentication renewAccessToken(RefreshToken refreshToken) {
        try {
            UUID.fromString(refreshToken.getToken());
        } catch (IllegalArgumentException e) {
            throw new InvalidRefreshTokenException(e.getMessage());
        }


        RefreshTokenEntity rte =
                refRepo.findRefreshTokenByToken(UUID.fromString(refreshToken.getToken()))
                        .orElseThrow(() -> new InvalidRefreshTokenException(
                                "Invalid refresh token"
                        ));

        if (rte.isRevoked())
            throw new InvalidRefreshTokenException(
                    "Use of revoked token"
            );

        if (rte.getExpiresAt().isBefore(Instant.now()))
            throw new InvalidRefreshTokenException(
                    "Token expired"
            );

        return new Authentication()
                .accessToken(jwtUtil.generateAccessToken(rte.getAccount()))
                .refreshToken(refreshToken.getToken());
    }
}
