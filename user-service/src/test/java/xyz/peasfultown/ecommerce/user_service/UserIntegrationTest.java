package xyz.peasfultown.ecommerce.user_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import xyz.peasfultown.ecommerce.user_api.model.NewAddressReq;
import xyz.peasfultown.ecommerce.user_api.model.NewUserReq;
import xyz.peasfultown.ecommerce.user_api.model.UpdateUserReq;
import xyz.peasfultown.ecommerce.user_service.entity.UserEntity;
import xyz.peasfultown.ecommerce.user_service.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
public class UserIntegrationTest {
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
    private UserRepository repo;

    private NewUserReq newUserReq;
    private UserEntity userEntity;
    private UpdateUserReq updateUserReq;
    private NewAddressReq newAddressReq;

    @BeforeEach
    void setup() {
        newUserReq = new NewUserReq()
                .id(UUID.randomUUID().toString())
                .email("someone@email.com")
                .firstName("First")
                .lastName("Last")
                .phone("1234567890");

        userEntity = UserEntity.builder()
                .id(UUID.fromString(newUserReq.getId()))
                .email("someone@email.com")
                .firstName("First")
                .lastName("Last")
                .phone("1234567890")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        updateUserReq = new UpdateUserReq()
                .email("otherEmail@email.com")
                .phone("0987654321");

        newAddressReq = new NewAddressReq()
                .number("42")
                .street("Maple Ave")
                .city("Austin")
                .state("Texas")
                .country("United States")
                .postalCode("78701");
    }

    @Test
    void createUser_shouldReturn201_whenValidInput() throws Exception {
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(newUserReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(newUserReq.getId()))
                .andExpect(jsonPath("$.email").value(newUserReq.getEmail()))
                .andExpect(jsonPath("$.firstName").value(newUserReq.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(newUserReq.getLastName()))
                .andExpect(jsonPath("$.phone").value(newUserReq.getPhone()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void createUser_shouldReturn400_whenEmailAlreadyExists() throws Exception {
        repo.save(userEntity);
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(newUserReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMyUser_shouldReturn200_whenValidInput() throws Exception {
        repo.save(userEntity);
        mvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(updateUserReq))
                        .header("X-User-Id", userEntity.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userEntity.getId().toString()))
                .andExpect(jsonPath("$.email").value(updateUserReq.getEmail()))
                .andExpect(jsonPath("$.phone").value(updateUserReq.getPhone()))
                .andExpect(jsonPath("$.firstName").value(userEntity.getFirstName()));
    }

    @Test
    void deleteMyUser_shouldReturn204_whenValidInput() throws Exception {
        repo.save(userEntity);

        mvc.perform(delete("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userEntity.getId().toString()))
                .andExpect(status().isNoContent());

        assertThat(repo.findById(userEntity.getId())).isNotPresent();
    }

    @Test
    void getMyUser_shouldReturn200_whenValidInput() throws Exception {
        repo.save(userEntity);
        mvc.perform(get("/api/v1/users/me")
                .header("X-User-Id", userEntity.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newUserReq.getId()))
                .andExpect(jsonPath("$.email").value(newUserReq.getEmail()))
                .andExpect(jsonPath("$.firstName").value(newUserReq.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(newUserReq.getLastName()))
                .andExpect(jsonPath("$.phone").value(newUserReq.getPhone()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void getMyUser_shouldReturn404_whenGivenNonExistentUserId() throws Exception {
        mvc.perform(get("/api/v1/users/me")
                .header("X-User-Id", userEntity.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMyAddress_shouldReturn201_whenValidInput() throws Exception {
        repo.save(userEntity);

        mvc.perform(post("/apo/v1/users/me/addresses")
                .header("X-User-Id", userEntity.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objMapper.writeValueAsString(newAddressReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(userEntity.getId().toString()))
                .andExpect(jsonPath("$.number").value(newAddressReq.getNumber()))
                .andExpect(jsonPath("$.street").value(newAddressReq.getStreet()))
                .andExpect(jsonPath("$.city").value(newAddressReq.getCity()))
                .andExpect(jsonPath("$.state").value(newAddressReq.getState()))
                .andExpect(jsonPath("$.country").value(newAddressReq.getCountry()))
                .andExpect(jsonPath("$.postalCode").value(newAddressReq.getPostalCode()))
                .andExpect(jsonPath("$.primary").isNotEmpty());
    }

    @Test
    void getAllMyAddresses_shouldReturn200_whenValidInput() throws Exception {
        // TODO: finish
    }

    @Test
    void getMyAddressById_shouldReturn200_whenValidInput() throws Exception {
        // TODO: finish
    }

    @Test
    void deleteMyAddressById_shouldReturn204_whenValidInput() throws Exception {
        // TODO: finish
    }

    @Test
    void updateMyAddressById_shouldReturn200_whenValidInput() throws Exception {
        // TODO: finish
    }

    @Test
    void getUsers_shouldReturn200_whenHasAdminRole() throws Exception {
        UserEntity u1 = UserEntity.builder()
                .email("someone1@email.com")
                .firstName("First")
                .lastName("Last")
                .phone("1234567890")
                .build();
        UserEntity u2 = UserEntity.builder()
                .email("someone2@email.com")
                .firstName("First")
                .lastName("Last")
                .phone("1234567891")
                .build();
        UserEntity u3 = UserEntity.builder()
                .email("someone3@email.com")
                .firstName("First")
                .lastName("Last")
                .phone("1234567892")
                .build();

        repo.saveAll(List.of(u1, u2, u3));

        mvc.perform(get("/api/v1/users")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", hasSize(3)));
    }

    @Test
    void getUsers_shouldReturn403_whenNotHasAdminRole() throws Exception {
        UserEntity u1 = UserEntity.builder()
                .email("someone1@email.com")
                .firstName("First")
                .lastName("Last")
                .phone("1234567890")
                .build();
        UserEntity u2 = UserEntity.builder()
                .email("someone2@email.com")
                .firstName("First")
                .lastName("Last")
                .phone("1234567891")
                .build();
        UserEntity u3 = UserEntity.builder()
                .email("someone3@email.com")
                .firstName("First")
                .lastName("Last")
                .phone("1234567892")
                .build();

        repo.saveAll(List.of(u1, u2, u3));

        mvc.perform(get("/api/v1/users")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void deleteUser_shouldReturn204_whenHasAdminRole() throws Exception {
        UserEntity u1 = UserEntity.builder()
                .email("someone1@email.com")
                .firstName("First")
                .lastName("Last")
                .phone("1234567890")
                .build();

        repo.save(u1);

        mvc.perform(delete("/api/v1/users/{id}", u1.getId().toString())
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());

        assertThat(repo.findById(u1.getId())).isNotPresent();
    }

    @Test
    void deleteUser_shouldReturn403_whenNotHasAdminRole() throws Exception {
        UserEntity u1 = UserEntity.builder()
                .email("someone1@email.com")
                .firstName("First")
                .lastName("Last")
                .phone("1234567890")
                .build();

        repo.save(u1);

        mvc.perform(delete("/api/v1/users/{id}", u1.getId().toString())
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden());

        assertThat(repo.findById(u1.getId())).isPresent();
    }


}
