package xyz.peasfultown.ecommerce.auth_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import xyz.peasfultown.ecommerce.auth_api.model.Authentication;
import xyz.peasfultown.ecommerce.auth_api.model.LoginReq;
import xyz.peasfultown.ecommerce.auth_api.model.NewAccountReq;
import xyz.peasfultown.ecommerce.auth_api.model.RefreshToken;
import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RefreshTokenEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RoleEnum;
import xyz.peasfultown.ecommerce.auth_service.repository.AuthRepository;
import xyz.peasfultown.ecommerce.auth_service.repository.RefreshTokenRepository;

import javax.print.attribute.standard.Media;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private AuthRepository auRepo;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passEncoder;

    private NewAccountReq req;
    private AccountEntity entity;

    @BeforeEach
    void setup() {
        req = new NewAccountReq()
                .email("someone@email.com")
                .password("password");
    }

    @Test
    void register_shouldReturn201_andReturnTokens_whenValidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void register_shouldSaveAccountToDatabase_whenValidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        Optional<AccountEntity> saved = auRepo.findAccountByEmail(req.getEmail());
        assertThat(saved).isPresent();
        assertThat(saved.get().getEmail()).isEqualTo(req.getEmail());
    }

    @Test
    void register_shouldEncodePassword_whenSavingToDatabase() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        AccountEntity saved = auRepo.findAccountByEmail(req.getEmail()).get();
        assertThat(saved.getPassword()).isNotEqualTo(req.getPassword());
        assertThat(passEncoder.matches(req.getPassword(), saved.getPassword()));
    }

    @Test
    void register_shouldThrow400_whenEmailAlreadyExists() throws Exception {
        AccountEntity existing = AccountEntity.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .build();

        auRepo.save(existing);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldThrow400_whenEmailIsBlank() throws Exception {
        req.email("");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldThrow400_whenPasswordIsBlank() throws Exception {
        req.password("");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnTokens_whenValidInput() throws Exception {
        AccountEntity ae = AccountEntity.builder()
                .email(req.getEmail())
                .role(RoleEnum.USER)
                .password(passEncoder.encode(req.getPassword()))
                .build();

        auRepo.save(ae);

        LoginReq lreq = new LoginReq()
                .email(req.getEmail())
                        .password(req.getPassword());

        mockMvc.perform(post("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objMapper.writeValueAsString(lreq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_shouldReturn400_whenWrongPassword() throws Exception {
        AccountEntity ae = AccountEntity.builder()
                .email(req.getEmail())
                .role(RoleEnum.USER)
                .password(passEncoder.encode(req.getPassword()))
                .build();

        auRepo.save(ae);

        LoginReq lreq = new LoginReq()
                .email(req.getEmail())
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
        String result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Authentication auth = objMapper.readValue(result, Authentication.class);

        mockMvc.perform(post("/api/v1/auth/token/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objMapper.writeValueAsString(auth.getRefreshToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void renewAccessToken_shouldReturn400_whenFalseRefreshToken() throws Exception {
        RefreshToken token = new RefreshToken()
                .token("invalidToken");
        mockMvc.perform(post("/api/v1/auth/token/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void renewAccessToken_shouldReturn400_whenExpiredRefreshToken() throws Exception {
        AccountEntity ae = AccountEntity.builder()
                .email(req.getEmail())
                .password(req.getPassword())
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
}
