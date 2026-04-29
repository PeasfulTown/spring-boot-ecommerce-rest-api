package xyz.peasfultown.ecommerce.auth_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import xyz.peasfultown.ecommerce.auth_api.model.Authentication;
import xyz.peasfultown.ecommerce.auth_api.model.LoginReq;
import xyz.peasfultown.ecommerce.auth_api.model.NewAccountReq;
import xyz.peasfultown.ecommerce.auth_api.model.RefreshToken;
import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RefreshTokenEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RoleEnum;
import xyz.peasfultown.ecommerce.auth_service.repository.AuthRepository;
import xyz.peasfultown.ecommerce.auth_service.repository.RefreshTokenRepository;
import xyz.peasfultown.ecommerce.auth_service.service.RefreshTokenService;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableWireMock({
        @ConfigureWireMock(
                name = "user-service",
                baseUrlProperties = "USER_SERVICE_URL",
                portProperties = "user-service.port"
        )
})
@ActiveProfiles("test")
@Transactional
@Testcontainers
public class IntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8")
            .withDatabaseName("ecommerce_auth_testdb")
            .withUsername("testuser")
            .withPassword("testpassword");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @InjectWireMock("user-service")
    private WireMockServer userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private AuthRepository auRepo;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passEncoder;

    @Autowired
    private SecretKey signingKey;

    private AccountEntity entity;

    @BeforeEach
    void setup() {
    }

    void stub_userService_createUser() {
        userService.stubFor(WireMock.post("/api/v1/users")
                .withRequestBody(matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)));
    }

    @Test
    void register_shouldReturn201_andReturnTokens_whenValidRequest() throws Exception {
        NewAccountReq req = NewAccountReq.builder()
                .email("someone@email.com")
                .password("password")
                .firstName("Firstnamename")
                .lastName("Lastname")
                .build();

        stub_userService_createUser();
        String json = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        AccountEntity ae = auRepo.findAccountByEmail(req.getEmail()).orElseThrow();
        assertTrue(passEncoder.matches(req.getPassword(), ae.getPassword()));
        List<RefreshTokenEntity> rtes = refreshTokenRepository.findRefreshTokensByAccountId(ae.getId());
        assertEquals(1, rtes.size());

        Authentication res = objMapper.readValue(json, Authentication.class);
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(res.getAccessToken())
                .getPayload();

        assertEquals(ae.getId().toString(), claims.getSubject());
        assertEquals(req.getEmail(), claims.get("email"));
        assertEquals("ROLE_USER", claims.get("role"));
    }

    @Test
    void register_shouldThrow400_whenEmailAlreadyExists() throws Exception {
        NewAccountReq req = NewAccountReq.builder()
                .email("someone@email.com")
                .password("password")
                .firstName("Firstname")
                .lastName("Lastname")
                .build();

        AccountEntity existing = AccountEntity.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .build();

        auRepo.save(existing);

        stub_userService_createUser();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnTokens_whenValidInput() throws Exception {
        AccountEntity ae = AccountEntity.builder()
                .email("user@example.com")
                .role(RoleEnum.USER)
                .password(passEncoder.encode("password"))
                .build();
        auRepo.save(ae);
        LoginReq req = LoginReq.builder()
                .email("user@example.com")
                .password("password")
                .build();

        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        List<RefreshTokenEntity> rtes = refreshTokenRepository.findRefreshTokensByAccountId(ae.getId());
        assertEquals(1, rtes.size());
    }

    @Test
    void login_shouldReturn400_whenWrongPassword() throws Exception {
        AccountEntity ae = AccountEntity.builder()
                .email("user@example.com")
                .role(RoleEnum.USER)
                .password(passEncoder.encode("password"))
                .build();

        auRepo.save(ae);

        LoginReq lreq = new LoginReq()
                .email("user@example.com")
                .password("wrongpass");

        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(lreq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void renewAccessToken_shouldReturn200_whenValidRefreshToken() throws Exception {
        AccountEntity ae = AccountEntity.builder()
                .email("user@example.com")
                .password(passEncoder.encode("password"))
                .role(RoleEnum.USER)
                .build();
        auRepo.save(ae);

        RefreshTokenEntity rte = refreshTokenService.createRefreshToken(ae);
        RefreshToken req = new RefreshToken(rte.getToken().toString());

        mockMvc.perform(post("/api/v1/auth/token/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        rte = refreshTokenRepository.findById(rte.getId()).orElseThrow();
        assertTrue(rte.isRevoked());
    }

    @Test
    void renewAccessToken_shouldReturn400_whenFalseRefreshToken() throws Exception {
        AccountEntity ae = AccountEntity.builder()
                .email("user@example.com")
                .password(passEncoder.encode("password"))
                .build();
        auRepo.save(ae);

        RefreshTokenEntity rte = refreshTokenService.createRefreshToken(ae);
        RefreshToken req = new RefreshToken(UUID.randomUUID().toString());

        mockMvc.perform(post("/api/v1/auth/token/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void renewAccessToken_shouldReturn400_whenExpiredRefreshToken() throws Exception {
        AccountEntity ae = AccountEntity.builder()
                .email("user@example.com")
                .password(passEncoder.encode("password"))
                .role(RoleEnum.USER)
                .build();
        ae = auRepo.save(ae);
        RefreshTokenEntity rte = RefreshTokenEntity.builder()
                .token(UUID.randomUUID())
                .account(ae)
                .createdAt(Instant.now().minusMillis(900000))
                .expiresAt(Instant.now().minusMillis(900))
                .revoked(false)
                .build();

        refreshTokenRepository.save(rte);
        RefreshToken refReq = new RefreshToken()
                .token(rte.getToken().toString());
        mockMvc.perform(post("/api/v1/auth/token/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(refReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void renewAccessToken_shouldReturn400_whenUsingRevokedRefreshToken() throws Exception {

        AccountEntity ae = AccountEntity.builder()
                .email("user@example.com")
                .password(passEncoder.encode("password"))
                .role(RoleEnum.USER)
                .build();
        ae = auRepo.save(ae);
        RefreshTokenEntity rte = RefreshTokenEntity.builder()
                .token(UUID.randomUUID())
                .account(ae)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(900000))
                .revoked(true)
                .build();
        refreshTokenRepository.save(rte);

        RefreshToken req = new RefreshToken()
                .token(rte.getToken().toString());
        mockMvc.perform(post("/api/v1/auth/token/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
