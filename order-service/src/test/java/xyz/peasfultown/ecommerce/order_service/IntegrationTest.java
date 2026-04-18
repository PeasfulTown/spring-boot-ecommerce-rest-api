package xyz.peasfultown.ecommerce.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;
import xyz.peasfultown.ecommerce.order_service.entity.OrderItemEntity;
import xyz.peasfultown.ecommerce.order_service.repository.OrderItemRepository;
import xyz.peasfultown.ecommerce.order_service.repository.OrderRepository;
import xyz.peasfultown.ecommerce.order_service.service.OrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
            .withDatabaseName("ecommerce_order_testdb")
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
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRepository oiRepo;

    @Autowired
    private OrderService orderService;

    @Test
    void getMyOrders_returns200_whenValidInputs() throws Exception {
        OrderEntity oe = OrderEntity.builder()
                .userId(UUID.randomUUID())
                .email("example@email.com")
                .phone("1234567890")
                .number("123")
                .street("Street St")
                .city("City")
                .state("State")
                .country("Country")
                .postalCode("123ABC")
                .totalPrice(BigDecimal.valueOf(333.33))
                .status(OrderEntity.OrderStatus.PROCESSING)
                .build();
        oe = orderRepo.save(oe);

        OrderItemEntity oie1 = OrderItemEntity.builder()
                .order(oe)
                .productId(UUID.randomUUID())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(111.11))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(111.11))
                .build();
        OrderItemEntity oie2 = OrderItemEntity.builder()
                .order(oe)
                .productId(UUID.randomUUID())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(222.22))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();

        oiRepo.saveAll(List.of(oie1, oie2));
        oe.getItems().addAll(List.of(oie1, oie2));
        orderRepo.save(oe);

        mvc.perform(get("/api/v1/orders")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                .header("X-User-Id", oe.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].items", hasSize(2)))
                .andExpect(jsonPath("$.content[0].items[0].productId", notNullValue()))
                .andExpect(jsonPath("$.page.totalElements", is(1)));
    }

}
