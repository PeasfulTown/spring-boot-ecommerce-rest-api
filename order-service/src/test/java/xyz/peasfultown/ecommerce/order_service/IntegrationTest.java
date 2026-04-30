package xyz.peasfultown.ecommerce.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import xyz.peasfultown.ecommerce.order_api.model.Order;
import xyz.peasfultown.ecommerce.order_api.model.OrderStatus;
import xyz.peasfultown.ecommerce.order_api.model.OrderUpdateRequest;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;
import xyz.peasfultown.ecommerce.order_service.entity.OrderItemEntity;
import xyz.peasfultown.ecommerce.order_service.repository.OrderItemRepository;
import xyz.peasfultown.ecommerce.order_service.repository.OrderRepository;
import xyz.peasfultown.ecommerce.order_service.service.OrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@EnableWireMock(
    @ConfigureWireMock(
            name = "user-sevice",
            baseUrlProperties = "USER_SERVICE_URL",
            portProperties = "user-service.port"
    )
)
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Slf4j
public class IntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper oMapper;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRepository oiRepo;

    @Autowired
    private OrderService orderService;

    private UUID userId1;
    private UUID userId2;

    private OrderItemEntity o1_i1;
    private OrderItemEntity o1_i2;
    private OrderItemEntity o1_i3;
    private OrderEntity order1;

    private OrderItemEntity o2_i1;
    private OrderItemEntity o2_i2;
    private OrderEntity order2;

    private OrderItemEntity o3_i1;
    private OrderItemEntity o3_i2;
    private OrderEntity order3;

    private OrderItemEntity o4_i1;
    private OrderItemEntity o4_i2;
    private OrderEntity order4;


    @BeforeEach
    void setup() {
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();

        o1_i1 = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(111.11))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(111.11))
                .build();
        o1_i2 = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(222.22))
                .quantity(2)
                .subtotal(BigDecimal.valueOf(444.44))
                .build();
        o1_i3 = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Product 3")
                .productPrice(BigDecimal.valueOf(333.33))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(333.33))
                .build();
        order1 = OrderEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId1)
                .fullName("Full Name 1")
                .email("example1@email.com")
                .phone("1234567890")
                .streetNumber("123")
                .streetName("Street St")
                .city("City")
                .state("State")
                .country("Country")
                .postalCode("123ABC")
                .status(OrderEntity.OrderStatus.PROCESSING)
                .totalPrice(BigDecimal.valueOf(888.88))
                .build();
        order1.addItems(List.of(o1_i1, o1_i2, o1_i3));

        o2_i1 = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(444.44))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(444.44))
                .build();
        o2_i2 = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(555.55))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(555.55))
                .build();
        order2 = OrderEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId1)
                .fullName("Full Name 1")
                .email("example1@email.com")
                .phone("1234567891")
                .streetNumber("234")
                .streetName("Street St")
                .city("City")
                .state("State")
                .country("Country")
                .postalCode("124ABC")
                .totalPrice(BigDecimal.valueOf(999.99))
                .status(OrderEntity.OrderStatus.PROCESSING)
                .build();
        order2.addItems(List.of(o2_i1, o2_i2));

        o3_i1 = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(444.44))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(444.44))
                .build();
        o3_i2 = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(555.55))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(555.55))
                .build();
        order3 = OrderEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId1)
                .fullName("Full Name 1")
                .email("example1@email.com")
                .phone("1234567891")
                .streetNumber("234")
                .streetName("Street St")
                .city("City")
                .state("State")
                .country("Country")
                .postalCode("124ABC")
                .totalPrice(BigDecimal.valueOf(999.99))
                .status(OrderEntity.OrderStatus.PROCESSING)
                .build();
        order3.addItems(List.of(o3_i1, o3_i2));

        o4_i1 = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(444.44))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(444.44))
                .build();
        o4_i2 = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(555.55))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(555.55))
                .build();
        order4 = OrderEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId2)
                .fullName("Full Name 1")
                .email("example1@email.com")
                .phone("1234567891")
                .streetNumber("234")
                .streetName("Street St")
                .city("City")
                .state("State")
                .country("Country")
                .postalCode("124ABC")
                .totalPrice(BigDecimal.valueOf(999.99))
                .status(OrderEntity.OrderStatus.PROCESSING)
                .build();
        order4.addItems(List.of(o4_i1, o4_i2));

        orderRepo.saveAll(List.of(order1, order2, order3, order4));

        assertTrue(orderRepo.count() == 4);
    }

    @Test
    void getAllOrders_returnsAllOrders_whenIsAdmin() throws Exception {
        mvc.perform(get("/api/v1/orders")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)));
    }

    @Test
    void getAllOrders_returnsOnlyOrdersTheUserOwns_andIsNotAdmin() throws Exception {
        mvc.perform(get("/api/v1/orders")
                        .header("X-User-Id", userId2)
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getOrderById_returns200_whenUserOwnsTheOrder_andIsNotAdmin() throws Exception {
        mvc.perform(get("/api/v1/orders/{id}", order1.getId().toString())
                        .header("X-User-Id", userId1.toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId1.toString()))
        ;
    }

    @Test
    void getOrderById_returns200_whenUserDoesntOwnTheOrder_andIsAdmin() throws Exception {
        mvc.perform(get("/api/v1/orders/{id}", order1.getId().toString())
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId1.toString()))
        ;
    }

    @Test
    void getOrderById_returns404_whenUserDoesntOwnTheOrder_andIsNotAdmin() throws Exception {
        mvc.perform(get("/api/v1/orders/{id}", order1.getId().toString())
                        .header("X-User-Id", userId2.toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNotFound())
                .andDo(print())
        ;
    }

    @Test
    void getOrderById_returns404_whenOrderNotExist() throws Exception {
        mvc.perform(get("/api/v1/orders/{id}", UUID.randomUUID().toString())
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound())
                .andDo(print())
        ;
    }

    @Test
    void getOrdersByUserId_returns200_whenIsAdmin() throws Exception {
        mvc.perform(get("/api/v1/orders/user/{id}", userId1.toString())
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
        ;
    }

    @Test
    void getOrdersByUserId_returns403_whenNotAdmin() throws Exception {
        mvc.perform(get("/api/v1/orders/user/{id}", userId1.toString())
                        .header("X-User-Id", userId2.toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden())
                .andDo(print())
        ;
    }

    @Test
    void updateOrderStatus_returns204_whenIsAdmin() throws Exception {
        OrderUpdateRequest req = OrderUpdateRequest.builder()
                .orderStatus(OrderStatus.CANCELLED)
                .build();
        mvc.perform(patch("/api/v1/orders/{id}/status", order1.getId().toString())
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                )
                .andExpect(status().isNoContent())
        ;

        OrderEntity oe = orderRepo.findById(order1.getId()).get();
        assertEquals(req.getOrderStatus(), OrderStatus.fromValue(oe.getStatus().getValue()));
    }

    @Test
    void updateOrderStatus_returns404_whenOrderNotExist() throws Exception {
        OrderUpdateRequest req = OrderUpdateRequest.builder()
                .orderStatus(OrderStatus.CANCELLED)
                .build();
        mvc.perform(patch("/api/v1/orders/{id}/status", UUID.randomUUID().toString())
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                )
                .andExpect(status().isNotFound())
                .andDo(print())
        ;
    }

    @Test
    void updateOrderStatus_returns403_whenNotAdmin() throws Exception {
        OrderUpdateRequest req = OrderUpdateRequest.builder()
                .orderStatus(OrderStatus.CANCELLED)
                .build();
        mvc.perform(patch("/api/v1/orders/{id}/status", order1.getId().toString())
                        .header("X-User-Id", userId1.toString())
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                )
                .andExpect(status().isForbidden())
                .andDo(print())
        ;

        OrderEntity oe = orderRepo.findById(order1.getId()).get();
        assertEquals(OrderStatus.PROCESSING, OrderStatus.fromValue(oe.getStatus().getValue()));
    }
}
