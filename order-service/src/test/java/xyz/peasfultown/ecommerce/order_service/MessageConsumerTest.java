package xyz.peasfultown.ecommerce.order_service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import xyz.peasfultown.ecommerce.order_api.model.CartItem;
import xyz.peasfultown.ecommerce.order_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.order_service.dto.OrderSubmission;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;
import xyz.peasfultown.ecommerce.order_service.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Testcontainers
@Slf4j
public class MessageConsumerTest {
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
        registry.add("spring.datasource.name", mysql::getDatabaseName);
    }

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static RabbitAdmin rabbitAdmin;

    @Autowired
    private void setRabbitAdmin(RabbitAdmin rabbitAdmin) {
        MessageConsumerTest.rabbitAdmin = rabbitAdmin;
    }

    @Test
    void consumeSubmittedOrderQueue_savesToDb() throws Exception {
        CartItem ci1 = CartItem.builder()
                .id(UUID.randomUUID().toString())
                .productId(UUID.randomUUID().toString())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(111.11))
                .quantity(2)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        CartItem ci2 = CartItem.builder()
                .id(UUID.randomUUID().toString())
                .productId(UUID.randomUUID().toString())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(222.22))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        OrderSubmission message = OrderSubmission.builder()
                .userId(UUID.randomUUID().toString())
                .contactEmail("email@example.com")
                .contactPhone("1234567899")
                .addressNumber("123")
                .addressStreet("Street St")
                .addressCity("City")
                .addressState("State")
                .addressCountry("Country")
                .addressPostalCode("111AAA")
                .orderTotal(BigDecimal.valueOf(444.44))
                .orderItemCount(3)
                .items(List.of(ci1, ci2))
                .build();

        log.info("Sending message with UserID: {}", message.getUserId());
        rabbitTemplate.convertAndSend(RabbitMqConstants.orderSubmitted_routingKey, message);
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> orderRepo.count() > 0);
        log.info("Querying for orders for userId: {}", UUID.fromString(message.getUserId()));
        List<OrderEntity> list = orderRepo.findOrdersByUserId(UUID.fromString(message.getUserId()));
        assertThat(list).isNotEmpty();
        log.info("List size: {}", list.size());
        OrderEntity oe = list.get(0);
        assertEquals(message.getUserId(), oe.getUserId().toString());
        assertEquals(message.getContactEmail(), oe.getEmail());
        assertEquals(message.getContactPhone(), oe.getPhone());
        assertEquals(message.getAddressNumber(), oe.getNumber());
        assertEquals(message.getAddressStreet(), oe.getStreet());
        assertEquals(message.getAddressCity(), oe.getCity());
        assertEquals(message.getAddressState(), oe.getState());
        assertEquals(message.getAddressCountry(), oe.getCountry());
        assertEquals(message.getOrderTotal(), oe.getTotalPrice());
        assertEquals(message.getOrderItemCount(), oe.getItemCount());
        assertEquals(2, oe.getItems().size());

        assertNotNull(oe.getItems().get(0).getId());
        assertNotNull(oe.getItems().get(0).getProductId());
        assertNotNull(oe.getItems().get(0).getProductName());
        assertNotNull(oe.getItems().get(0).getProductPrice());
        assertNotNull(oe.getItems().get(0).getQuantity());
        assertNotNull(oe.getItems().get(0).getSubtotal());
        assertNotNull(oe.getItems().get(1).getId());
        assertNotNull(oe.getItems().get(1).getProductId());
        assertNotNull(oe.getItems().get(1).getProductName());
        assertNotNull(oe.getItems().get(1).getProductPrice());
        assertNotNull(oe.getItems().get(1).getQuantity());
        assertNotNull(oe.getItems().get(1).getSubtotal());
    }

}
