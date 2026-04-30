package xyz.peasfultown.ecommerce.user_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import xyz.peasfultown.ecommerce.user_api.model.*;
import xyz.peasfultown.ecommerce.user_service.entity.AddressEntity;
import xyz.peasfultown.ecommerce.user_service.entity.CardEntity;
import xyz.peasfultown.ecommerce.user_service.entity.UserEntity;
import xyz.peasfultown.ecommerce.user_service.repository.AddressRepository;
import xyz.peasfultown.ecommerce.user_service.repository.CardRepository;
import xyz.peasfultown.ecommerce.user_service.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AddressRepository addrRepo;

    @Autowired
    private CardRepository cardRepo;

    @Value("${services.internal-secret}")
    private String internalSecret;

    private UserEntity ue1;
    private UserEntity ue2;

    private AddressEntity addr1;
    private AddressEntity addr2;
    private AddressEntity addr3;
    private AddressEntity addr4;

    @BeforeEach
    void setup() {
        ue1 = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("user1@email.com")
                .firstName("First1")
                .lastName("Last1")
                .phone("1234567890")
                .build();
        ue2 = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("user2@email.com")
                .firstName("First2")
                .lastName("Last2")
                .phone("1234567890")
                .build();

        addr1 = AddressEntity.builder()
                .number("123")
                .street("Street St1")
                .city("City1")
                .state("State1")
                .country("Country1")
                .postalCode("Postal1")
                .isPrimary(false)
                .build();

        addr2 = AddressEntity.builder()
                .number("456")
                .street("Street St11")
                .city("City2")
                .state("State2")
                .country("Country2")
                .postalCode("Postal2")
                .isPrimary(false)
                .build();

        addr3 = AddressEntity.builder()
                .number("789")
                .street("Street St3")
                .city("City3")
                .state("State3")
                .country("Country3")
                .postalCode("postal3")
                .isPrimary(false)
                .build();


        addr4 = AddressEntity.builder()
                .number("123")
                .street("Street Ave 4")
                .city("City4")
                .state("State4")
                .country("Country 4")
                .postalCode("postal4")
                .isPrimary(false)
                .build();

        ue1.addAddress(addr1);
        ue2.addAddresses(List.of(addr2, addr3, addr4));
        userRepo.saveAll(List.of(ue1, ue2));
    }

    @Test
    void createUser_shouldReturn201_whenValidInput() throws Exception {
        UserCreateRequest req = UserCreateRequest.builder()
                .id(UUID.randomUUID().toString())
                .email("user3@example.com")
                .firstName("First3")
                .lastName("Last3")
                .phone("123457893")
        .build();
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                ;

        UserEntity ue = userRepo.findUserByEmail(req.getEmail()).orElseThrow();
        assertEquals(req.getEmail(), ue.getEmail());
        assertEquals(req.getFirstName(), ue.getFirstName());
        assertEquals(req.getLastName(), ue.getLastName());
        assertEquals(req.getPhone(), ue.getPhone());
    }

    @Test
    void createUser_shouldReturn400_whenEmailAlreadyExists() throws Exception {
        UserCreateRequest req = UserCreateRequest.builder()
                .email("user3@example.com")
                .firstName("First3")
                .lastName("Last3")
                .phone("123457893")
                .build();

        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_returns200_whenAdmin() throws Exception {
        mvc.perform(get("/api/v1/users")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
        ;
    }

    @Test
    void getUsers_returns403_whenNotAdmin() throws Exception {
        mvc.perform(get("/api/v1/users")
                .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden())
                ;
    }

    @Test
    void getUser_returns200_whenValidRequest() throws Exception {
        mvc.perform(get("/api/v1/users/{userId}", ue1.getId().toString())
                .header("X-User-Id", ue1.getId())
                .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ue1.getId().toString()))
                .andExpect(jsonPath("$.email").value(ue1.getEmail()))
                .andExpect(jsonPath("$.firstName").value(ue1.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(ue1.getLastName()))
                .andExpect(jsonPath("$.phone").value(ue1.getPhone()))
                ;
    }

    @Test
    void getUser_returns200_whenUserIdHeaderNotSameAsRequestUserId_AndIsAdmin() throws Exception {
        mvc.perform(get("/api/v1/users/{userId}", ue1.getId().toString())
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
        ;
    }

    @Test
    void getUser_returns403_whenUserIdHeaderNotSameAsRequestedUserId_andNotAdmin() throws Exception {
        mvc.perform(get("/api/v1/users/{userId}", ue1.getId().toString())
                        .header("X-User-Id", ue2.getId())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden())
                ;
    }

    @Test
    void updateUser_returns200_whenValidRequest() throws Exception {
        UserUpdateRequest req = UserUpdateRequest.builder()
                .email("new@example.com")
                .firstName("NewFirst")
                .lastName("NewLast")
                .phone("123457899")
        .build();

        mvc.perform(patch("/api/v1/users/{userId}", ue1.getId().toString())
                .header("X-User-Id", ue1.getId().toString())
                        .header("X-User-Role", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        UserEntity ue = userRepo.findById(ue1.getId()).orElseThrow();
        assertEquals(req.getEmail(), ue.getEmail());
        assertEquals(req.getFirstName(), ue.getFirstName());
        assertEquals(req.getLastName(), ue.getLastName());
        assertEquals(req.getPhone(), ue.getPhone());
    }

    @Test
    void updateUser_returns200_whenIsAdmin() throws Exception {
        UserUpdateRequest req = UserUpdateRequest.builder()
                .email("new@example.com")
                .firstName("NewFirst")
                .lastName("NewLast")
                .phone("123457899")
                .build();

        mvc.perform(patch("/api/v1/users/{userId}", ue1.getId().toString())
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        UserEntity ue = userRepo.findById(ue1.getId()).orElseThrow();
        assertEquals(req.getEmail(), ue.getEmail());
        assertEquals(req.getFirstName(), ue.getFirstName());
        assertEquals(req.getLastName(), ue.getLastName());
        assertEquals(req.getPhone(), ue.getPhone());
    }

    @Test
    void updateUser_returns403_whenUserIdHeaderNotSameAsRequestedUserId_andNotAdmin() throws Exception {
        UserUpdateRequest req = UserUpdateRequest.builder()
                .email("new@example.com")
                .firstName("NewFirst")
                .lastName("NewLast")
                .phone("123457899")
                .build();

        mvc.perform(patch("/api/v1/users/{userId}", ue1.getId().toString())
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_returns404_whenUserNotFound() throws Exception {
        UUID randUserId = UUID.randomUUID();
        UserUpdateRequest req = UserUpdateRequest.builder()
                .email("new@example.com")
                .firstName("NewFirst")
                .lastName("NewLast")
                .phone("123457899")
                .build();

        mvc.perform(patch("/api/v1/users/{userId}", randUserId.toString())
                        .header("X-User-Id", randUserId.toString())
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_returns204_whenValidRequest() throws Exception {
        mvc.perform(delete("/api/v1/users/{userId}", ue1.getId().toString())
                .header("X-User-Id", ue1.getId().toString())
                .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNoContent())
                ;

        Optional<UserEntity> ue = userRepo.findById(ue1.getId());
        assertThat(ue).isEmpty();
    }

    @Test
    void deleteUser_returns204_whenUserIdHeaderNotSameAsRequestedUserId_andIsAdmin() throws Exception {
        mvc.perform(delete("/api/v1/users/{userId}", ue1.getId().toString())
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent())
        ;
    }

    @Test
    void deleteUser_returns403_whenUserIdHeaderNotSameAsRequestedUserId_andNotAdmin() throws Exception {
        mvc.perform(delete("/api/v1/users/{userId}", ue2.getId().toString())
                        .header("X-User-Id", ue1.getId().toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void deleteUser_returns404_whenUserNotFound() throws Exception {
        UUID randUserId = UUID.randomUUID();
        mvc.perform(delete("/api/v1/users/{userId}", randUserId.toString())
                        .header("X-User-Id", randUserId.toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    void getOrderInformation_returns200_whenValidRequest() throws Exception {
        OrderInformationRequest req = OrderInformationRequest.builder()
                .userId(ue1.getId().toString())
                .addressId(addr1.getId().toString())
        .build();

        mvc.perform(post("/api/v1/users/order-info")
                .header("X-Internal-Service-Secret", internalSecret)
                .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(ue1.getId().toString()))
                .andExpect(jsonPath("$.fullName").value(ue1.getFirstName() + " " + ue1.getLastName()))
                .andExpect(jsonPath("$.email").value(ue1.getEmail()))
                .andExpect(jsonPath("$.phone").value(ue1.getPhone()))
                .andExpect(jsonPath("$.address.id").value(addr1.getId().toString()))
                .andExpect(jsonPath("$.address.streetNumber").value(addr1.getNumber()))
                .andExpect(jsonPath("$.address.streetName").value(addr1.getStreet()))
                .andExpect(jsonPath("$.address.city").value(addr1.getCity()))
                .andExpect(jsonPath("$.address.state").value(addr1.getState()))
                .andExpect(jsonPath("$.address.country").value(addr1.getCountry()))
                .andExpect(jsonPath("$.address.postalCode").value(addr1.getPostalCode()))
                ;
    }

    @Test
    void getOrderInformation_returns403_whenIncorrectInternalServiceSecret() throws Exception {
        OrderInformationRequest req = OrderInformationRequest.builder()
                .userId(ue1.getId().toString())
                .addressId(addr1.getId().toString())
                .build();

        mvc.perform(post("/api/v1/users/order-info")
                        .header("X-Internal-Service-Secret", "someOtherSecret")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrderInformation_returns403_whenNotAdmin() throws Exception {
        OrderInformationRequest req = OrderInformationRequest.builder()
                .userId(ue1.getId().toString())
                .addressId(addr1.getId().toString())
                .build();

        mvc.perform(post("/api/v1/users/order-info")
                        .header("X-Internal-Service-Secret", internalSecret)
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPaymentCard_returns201_whenValidRequest() throws Exception {
        CardCreateRequest req = CardCreateRequest.builder()
                .cardHolderName("John Smith")
                .cardNumber("4242424242424242")
                .cvv(123)
                .expiryMonth(3)
                .expiryYear(2028)
                .build();
        mvc.perform(post("/api/v1/users/{userId}/cards", ue1.getId().toString())
                .header("X-User-Id", ue1.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.cardHolderName").value(req.getCardHolderName()))
                .andExpect(jsonPath("$.lastFourDigits").value(req.getCardNumber().substring(req.getCardNumber().length() - 4)))
                .andExpect(jsonPath("$.expiryMonth").value(req.getExpiryMonth()))
                .andExpect(jsonPath("$.expiryYear").value(req.getExpiryYear()))
                ;
        List<CardEntity> ces = cardRepo.findCardsByUserId(ue1.getId());
        assertThat(ces).isNotEmpty();
        CardEntity ce = ces.get(0);
        assertEquals(req.getCardHolderName(), ce.getCardHolderName());
        assertEquals(req.getExpiryMonth(), ce.getExpiryMonth());
        assertEquals(req.getExpiryYear(), ce.getExpiryYear());
        assertEquals(CardEntity.CardType.VISA, ce.getCardType());
        assertEquals("4242", ce.getLastFourDigits());
        assertNotNull(ce.getToken());
    }

    @Test
    void getCard_returns200_whenValidRequest() throws Exception {
        CardEntity ce = CardEntity.builder()
                .cardHolderName("John Smith")
                .lastFourDigits("1234")
                .cardType(CardEntity.CardType.VISA)
                .expiryMonth(1)
                .expiryYear(2028)
                .token("tok_visa_1234_abcd1234")
                .user(ue1)
        .build();
        ce = cardRepo.save(ce);
        mvc.perform(get("/api/v1/users/{userId}/cards/{cardId}",
                    ue1.getId().toString(),
                    ce.getId().toString())
                .header("X-User-Id", ue1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ce.getId().toString()))
                .andExpect(jsonPath("$.cardHolderName").value(ce.getCardHolderName()))
                .andExpect(jsonPath("$.lastFourDigits").value(ce.getLastFourDigits()))
                .andExpect(jsonPath("$.cardType").value(ce.getCardType().getValue()))
                .andExpect(jsonPath("$.expiryMonth").value(ce.getExpiryMonth()))
                .andExpect(jsonPath("$.expiryYear").value(ce.getExpiryYear()))
                ;
    }

    @Test
    void getCard_returns403_whenUserDoesntOwnCard() throws Exception {
        CardEntity ce = CardEntity.builder()
                .cardHolderName("John Smith")
                .lastFourDigits("1234")
                .cardType(CardEntity.CardType.VISA)
                .expiryMonth(1)
                .expiryYear(2028)
                .token("tok_visa_1234_abcd1234")
                .user(ue1)
                .build();
        ce = cardRepo.save(ce);
        mvc.perform(get("/api/v1/users/{userId}/cards/{cardId}",
                        ue1.getId().toString(),
                        ce.getId().toString())
                        .header("X-User-Id", ue2.getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void setCardAsDefault_savesInDb() throws Exception {
        CardEntity ce1 = CardEntity.builder()
                .cardHolderName("John Smith")
                .lastFourDigits("1234")
                .cardType(CardEntity.CardType.VISA)
                .expiryMonth(1)
                .expiryYear(2028)
                .token("tok_visa_1234_abcd1234")
                .isDefault(true)
                .user(ue1)
                .build();
        CardEntity ce2 = CardEntity.builder()
                .cardHolderName("John Smith")
                .lastFourDigits("1235")
                .cardType(CardEntity.CardType.VISA)
                .expiryMonth(2)
                .expiryYear(2029)
                .token("tok_visa_1235_abcd1235")
                .isDefault(false)
                .user(ue1)
                .build();

        cardRepo.saveAll(List.of(ce1, ce2));
        mvc.perform(patch("/api/v1/users/{userId}/cards/{cardId}/default",
                ue1.getId().toString(),
                ce2.getId().toString())
                .header("X-User-Id", ue1.getId().toString()))
                .andExpect(status().isNoContent());

        CardEntity ce = cardRepo.findById(ce2.getId()).orElseThrow();
        assertTrue(ce.isDefault());
        ce = cardRepo.findById(ce1.getId()).orElseThrow();
        assertFalse(ce.isDefault());
    }

    @Test
    void deleteCard_deletesFromDb() throws Exception {
        CardEntity ce = CardEntity.builder()
                .cardHolderName("John Smith")
                .lastFourDigits("1234")
                .cardType(CardEntity.CardType.VISA)
                .expiryMonth(1)
                .expiryYear(2028)
                .token("tok_visa_1234_abcd1234")
                .user(ue1)
                .build();

        cardRepo.save(ce);
        mvc.perform(delete("/api/v1/users/{userId}/cards/{cardId}",
                        ue1.getId().toString(),
                        ce.getId().toString())
                .header("X-User-Id", ue1.getId().toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getCardToken_returns200_whenValidRequest() throws Exception {
        CardEntity ce = CardEntity.builder()
                .cardHolderName("John Smith")
                .lastFourDigits("1234")
                .cardType(CardEntity.CardType.VISA)
                .expiryMonth(1)
                .expiryYear(2028)
                .token("tok_visa_1234_abcd1234")
                .user(ue1)
                .build();
        cardRepo.save(ce);
        mvc.perform(get("/api/v1/users/cards/{cardId}/token", ce.getId().toString())
                .header("X-Internal-Service-Secret", internalSecret)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(ce.getId().toString()))
                .andExpect(jsonPath("$.token").value(ce.getToken()))
                .andExpect(jsonPath("$.cardType").value(ce.getCardType().getValue()))
                .andExpect(jsonPath("$.expiryMonth").value(ce.getExpiryMonth()))
                .andExpect(jsonPath("$.expiryYear").value(ce.getExpiryYear()))
                ;
    }
}
