package xyz.peasfultown.ecommerce.auth_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import xyz.peasfultown.ecommerce.auth_api.model.Authentication;
import xyz.peasfultown.ecommerce.auth_api.model.NewAccountReq;
import xyz.peasfultown.ecommerce.auth_service.auth.JwtUtil;
import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;
import xyz.peasfultown.ecommerce.auth_service.exception.AccountAlreadyExistsException;
import xyz.peasfultown.ecommerce.auth_service.repository.AuthRepository;
import xyz.peasfultown.ecommerce.auth_service.service.AuthServiceImpl;
import xyz.peasfultown.ecommerce.auth_service.service.RefreshTokenService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AuthServiceTest {
    @Mock
    private AuthRepository repo;

    @Mock
    private RefreshTokenService refService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl service;

    private NewAccountReq req;
    private AccountEntity entity;

    @BeforeEach
    void setUp() {
        req = NewAccountReq.builder()
                .email("someone@email.com")
                .password("password")
                .build();
        entity = AccountEntity.builder()
                .id(UUID.randomUUID())
                .email(req.getEmail())
                .password("encodedpassword")
                .build();
    }

    @Test
    void createNewAccount_returnsCorrectValues_whenCorrectInput() {
        when(repo.findAccountByEmail(req.getEmail())).thenReturn(Optional.ofNullable(null));
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encodedPassword");
        when(repo.save(any(AccountEntity.class))).thenReturn(entity);
        when(jwtUtil.generateAccessToken(entity)).thenReturn("mock-access-token");
        when(refService.createRefreshToken(entity)).thenReturn("mock-refresh-token");

        Authentication result = service.createNewAccount(req);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("mock-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("mock-refresh-token");
    }

    @Test
    void createNewAccount_shouldThrowException_whenEmailAlreadyExists() {
        when(repo.findAccountByEmail(req.getEmail())).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.createNewAccount(req))
                .isInstanceOf(AccountAlreadyExistsException.class)
                .hasMessageContaining(req.getEmail());
    }

}
