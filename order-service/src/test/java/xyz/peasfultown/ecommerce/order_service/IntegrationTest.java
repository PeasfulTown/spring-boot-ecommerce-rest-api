package xyz.peasfultown.ecommerce.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
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
import xyz.peasfultown.ecommerce.order_api.model.OrderItem;
import xyz.peasfultown.ecommerce.order_api.model.OrderStatus;
import xyz.peasfultown.ecommerce.order_api.model.UpdateOrderStatusReq;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;
import xyz.peasfultown.ecommerce.order_service.entity.OrderItemEntity;
import xyz.peasfultown.ecommerce.order_service.repository.OrderItemRepository;
import xyz.peasfultown.ecommerce.order_service.repository.OrderRepository;
import xyz.peasfultown.ecommerce.order_service.service.OrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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


    private OrderItemEntity oi1;
    private OrderItemEntity oi2;
    private OrderItemEntity oi3;
    private OrderEntity oe1;

    private OrderItemEntity oi4;
    private OrderItemEntity oi5;
    private OrderEntity oe2;

    @BeforeEach
    void setup() {
        UUID userId = UUID.randomUUID();
        oi1 = OrderItemEntity.builder()
                .order(oe1)
                .productId(UUID.randomUUID())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(111.11))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(111.11))
                .build();
        oi2 = OrderItemEntity.builder()
                .order(oe1)
                .productId(UUID.randomUUID())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(222.22))
                .quantity(2)
                .subtotal(BigDecimal.valueOf(444.44))
                .build();
        oi3 = OrderItemEntity.builder()
                .order(oe1)
                .productId(UUID.randomUUID())
                .productName("Product 3")
                .productPrice(BigDecimal.valueOf(333.33))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(333.33))
                .build();
        oe1 = OrderEntity.builder()
                .userId(userId)
                .email("example1@email.com")
                .phone("1234567890")
                .number("123")
                .street("Street St")
                .city("City")
                .state("State")
                .country("Country")
                .postalCode("123ABC")
                .status(OrderEntity.OrderStatus.PROCESSING)
                .items(List.of(oi1, oi2, oi3))
                .totalPrice(BigDecimal.valueOf(888.88))
                .build();
        oe1 = orderRepo.save(oe1);

        oi4 = OrderItemEntity.builder()
                .order(oe2)
                .productId(UUID.randomUUID())
                .productName("Product 4")
                .productPrice(BigDecimal.valueOf(444.44))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(444.44))
                .build();
        oi5 = OrderItemEntity.builder()
                .order(oe2)
                .productId(UUID.randomUUID())
                .productName("Product 5")
                .productPrice(BigDecimal.valueOf(555.55))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(555.55))
                .build();
        oe2 = OrderEntity.builder()
                .userId(userId)
                .email("example2@email.com")
                .phone("1234567891")
                .number("234")
                .street("Street St")
                .city("City")
                .state("State")
                .country("Country")
                .postalCode("124ABC")
                .items(List.of(oi4, oi5))
                .totalPrice(BigDecimal.valueOf(999.99))
                .status(OrderEntity.OrderStatus.PROCESSING)
                .build();
        oe2 = orderRepo.save(oe2);
    }

    @Test
    void getMyOrders_returns200() throws Exception {
        mvc.perform(get("/api/v1/orders")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .header("X-User-Id", oe1.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page.totalElements", is(2)));
    }

    @Test
    void getAllUsersOrders_returns200() throws Exception {
        mvc.perform(get("/api/v1/orders/all")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page.totalElements", is(2)));
    }

    @Test
    void getAllUsersOrders_returns403_ifNotAdmin() throws Exception {
        mvc.perform(get("/api/v1/orders/all")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyOrderById_returns200() throws Exception {
        mvc.perform(get("/api/v1/orders/{id}", oe1.getId())
                        .header("X-User-Id", oe1.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void getOrdersByStatus_returns200() throws Exception {
        mvc.perform(get("/api/v1/orders/status/{status}", OrderEntity.OrderStatus.PROCESSING.toString())
                        .header("X-User-Id", oe1.getUserId().toString())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].status", is("PROCESSING")));
    }

    @Test
    void getOrdersByStatus_returns403_ifNotAdmin() throws Exception {
        mvc.perform(get("/api/v1/orders/status/{status}", OrderEntity.OrderStatus.PROCESSING.toString())
                        .header("X-User-Id", oe1.getUserId().toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrdersByUserId_returns200() throws Exception {
        mvc.perform(get("/api/v1/orders/user/{id}", oe1.getUserId().toString())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getOrdersByUserId_returns403_ifNotAdmin() throws Exception {
        mvc.perform(get("/api/v1/orders/user/{id}", oe1.getUserId().toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrderStatus_savesCorrectRecord() throws Exception {
        mvc.perform(patch("/api/v1/orders/{id}/status", oe1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(new UpdateOrderStatusReq().orderStatus(OrderStatus.CANCELLED)))
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());
        assertEquals(OrderEntity.OrderStatus.CANCELLED, orderRepo.findById(oe1.getId()).get().getStatus());
    }

    @Test
    void updateOrderStatus_returns403_whenNotAdmin() throws Exception {
        mvc.perform(patch("/api/v1/orders/{id}/status", oe1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(new UpdateOrderStatusReq().orderStatus(OrderStatus.CANCELLED)))
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }
}
