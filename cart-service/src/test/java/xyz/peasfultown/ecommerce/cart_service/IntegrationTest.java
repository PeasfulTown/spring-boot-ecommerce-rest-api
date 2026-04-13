package xyz.peasfultown.ecommerce.cart_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
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
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import xyz.peasfultown.ecommerce.cart_api.model.AddItemReq;
import xyz.peasfultown.ecommerce.cart_api.model.Product;
import xyz.peasfultown.ecommerce.cart_service.repository.CartRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
@EnableWireMock({
        @ConfigureWireMock(
                name = "product-service",
                baseUrlProperties = "product-service.url",
                portProperties = "product-service.port"
        )
})
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


    @InjectWireMock("product-service")
    private WireMockServer productService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private CartRepository cartRepo;

    @Test
    void getMyCart_createsNewCartForUser_whenCartDoesntAlreadyExists() throws Exception {
        UUID userId = UUID.randomUUID();
        mvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());

        assertThat(cartRepo.findCartByUserid(userId)).isPresent();
    }

    @Test
    void addItemToCart_returns200_whenValidInput() throws Exception {
        UUID userId = UUID.randomUUID();
        Product prod = new Product()
                .id(UUID.randomUUID().toString())
                .name("iPhone 15")
                .description("Newest Apple product")
                .price(BigDecimal.valueOf(999.99))
                .imageUrls(List.of(
                        "http://www.images.com/products/iphone14_1.png",
                        "http://www.images.com/products/iphone14_2.png"
                        ));

        productService.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .get("/api/v1/products/" + prod.getId())
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(objMapper.writeValueAsString(prod))));

        AddItemReq req = new AddItemReq()
                .productId(prod.getId())
                        .quantity(2);
        mvc.perform(post("/api/v1/cart/items")
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

}
