package xyz.peasfultown.ecommerce.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import xyz.peasfultown.ecommerce.order_api.model.OrderItem;
import xyz.peasfultown.ecommerce.order_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.order_service.dto.*;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;
import xyz.peasfultown.ecommerce.order_service.entity.OrderItemEntity;
import xyz.peasfultown.ecommerce.order_service.repository.OrderItemRepository;
import xyz.peasfultown.ecommerce.order_service.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@EnableWireMock(
        @ConfigureWireMock(
                name = "user-service",
                baseUrlProperties = "USER_SERVICE_URL",
                portProperties = "user-service.port"
        )
)
@SpringBootTest
@ActiveProfiles({"test", "rabbitmq"})
@Slf4j
public class AsyncServiceFunctionTest {
    @InjectWireMock("user-service")
    private WireMockServer userService;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRepository oiRepo;

    @Autowired
    private ObjectMapper oMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    void stub_getUserInformationAndAddress_returns200(UserIdAndAddressIdRequest req, OrderInformation userInfo) throws Exception {
        userService.stubFor(WireMock.post(urlPathTemplate("/api/v1/users/order-info"))
                .withRequestBody(equalToJson(oMapper.writeValueAsString(req)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(oMapper.writeValueAsString(userInfo))));
    }

    @AfterEach
    void teardown() {
        orderRepo.deleteAll();
        rabbitAdmin.purgeQueue(RabbitMqConstants.payment_confirmPayment_queue);
    }

    @Test
    void consumeOrderCreateMessage_whenCartCheckout_savesInDb() throws Exception {
        OrderItem oi1 = OrderItem.builder()
                .productId(UUID.randomUUID().toString())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(111.11))
                .quantity(2)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        OrderItem oi2 = OrderItem.builder()
                .productId(UUID.randomUUID().toString())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(222.22))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        OrderCreateMessage orderMessage = OrderCreateMessage.builder()
                .userId(UUID.randomUUID().toString())
                .cardId(UUID.randomUUID().toString())
                .addressId(UUID.randomUUID().toString())
                .totalPrice(BigDecimal.valueOf(444.44))
                .itemCount(3)
                .items(List.of(oi1, oi2))
                .build();

        Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(orderMessage))
                .setHeader("__TypeId__", OrderCreateMessage.class.getSimpleName())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        UserIdAndAddressIdRequest req = new UserIdAndAddressIdRequest(orderMessage.getUserId(), orderMessage.getAddressId());
        OrderInformation userInfo = OrderInformation.builder()
                .userId(req.getUserId())
                .fullName("Full Name")
                .email("user@example.com")
                .phone("1234567890")
                .address(Address.builder()
                        .streetNumber("123")
                        .streetName("Street St")
                        .city("Some City")
                        .state("Some State")
                        .country("Some Country")
                        .postalCode("123ABC")
                        .build())
                .build();
        stub_getUserInformationAndAddress_returns200(req, userInfo);

        // send test message to test order service handler
        rabbitTemplate.send(RabbitMqConstants.cart_checkout_order_createOrder_routingKey, message);
        await().atMost(10, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> orderRepo.count() > 0);

        OrderEntity oe = Optional.ofNullable(orderRepo.findAll().get(0)).orElseThrow();
        assertEquals(orderMessage.getUserId(), oe.getUserId().toString());
        assertEquals(userInfo.getEmail(), oe.getEmail());
        assertEquals(userInfo.getPhone(), oe.getPhone());
        assertEquals(userInfo.getAddress().getStreetNumber(), oe.getStreetNumber());
        assertEquals(userInfo.getAddress().getStreetName(), oe.getStreetName());
        assertEquals(userInfo.getAddress().getCity(), oe.getCity());
        assertEquals(userInfo.getAddress().getState(), oe.getState());
        assertEquals(userInfo.getAddress().getCountry(), oe.getCountry());
        assertEquals(userInfo.getAddress().getPostalCode(), oe.getPostalCode());
        assertEquals(orderMessage.getTotalPrice(), oe.getTotalPrice());
        assertEquals(orderMessage.getItemCount(), oe.getItemCount());
        assertEquals(OrderEntity.OrderStatus.PROCESSING, oe.getStatus());

        List<OrderItemEntity> ois = oiRepo.findOrderItemsByOrderId(oe.getId());
        assertEquals(2, ois.size());
        Map<UUID, OrderItemEntity> orderItemMap = ois.stream().collect(Collectors.toMap(
                OrderItemEntity::getProductId, Function.identity()));

        orderMessage.getItems().forEach(i -> {
            OrderItemEntity oie = Optional.ofNullable(orderItemMap.get(UUID.fromString(i.getProductId()))).orElseThrow();
            assertEquals(i.getProductName(), oie.getProductName());
            assertEquals(i.getProductPrice(), oie.getProductPrice());
            assertEquals(i.getQuantity(), oie.getQuantity());
            assertEquals(i.getSubtotal(), oie.getSubtotal());
        });
    }

    @Test
    void consumeOrderCreateMessage_sendsPaymentConfirmationMessage() throws Exception {
        OrderItem oi1 = OrderItem.builder()
                .productId(UUID.randomUUID().toString())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(111.11))
                .quantity(2)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        OrderItem oi2 = OrderItem.builder()
                .productId(UUID.randomUUID().toString())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(222.22))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        OrderCreateMessage orderMessage = OrderCreateMessage.builder()
                .userId(UUID.randomUUID().toString())
                .cardId(UUID.randomUUID().toString())
                .addressId(UUID.randomUUID().toString())
                .totalPrice(BigDecimal.valueOf(444.44))
                .itemCount(3)
                .items(List.of(oi1, oi2))
                .build();
        Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(orderMessage))
                .setHeader("__TypeId__", OrderCreateMessage.class.getSimpleName())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        UserIdAndAddressIdRequest req = new UserIdAndAddressIdRequest(orderMessage.getUserId(), orderMessage.getAddressId());
        OrderInformation userInfo = OrderInformation.builder()
                .userId(req.getUserId())
                .fullName("Full Name")
                .email("user@example.com")
                .phone("1234567890")
                .address(Address.builder()
                        .streetNumber("123")
                        .streetName("Street St")
                        .city("Some City")
                        .state("Some State")
                        .country("Some Country")
                        .postalCode("123ABC")
                        .build())
                .build();
        stub_getUserInformationAndAddress_returns200(req, userInfo);

        rabbitTemplate.send(RabbitMqConstants.cart_checkout_order_createOrder_routingKey, message);

        PaymentConfirmationMessage sentMessage = rabbitTemplate.receiveAndConvert(RabbitMqConstants.payment_confirmPayment_queue,
                10_000, new ParameterizedTypeReference<PaymentConfirmationMessage>() {
                });

        assertNotNull(sentMessage);
        OrderEntity oe = orderRepo.findAll().get(0);
        assertEquals(oe.getId().toString(), sentMessage.getOrderId());
        assertEquals(orderMessage.getCardId(), sentMessage.getCardId());
    }

    @Test
    void consumeOrderConfirmationMessage_savesInDb() throws Exception {
        OrderItemEntity oie1 = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(111.11))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(111.11))
                .build();
        OrderItemEntity oie2 = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(222.22))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        OrderItemEntity oie3 = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .productName("Product 3")
                .productPrice(BigDecimal.valueOf(333.33))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(333.33))
                .build();
        OrderEntity oe = OrderEntity.builder()
                .userId(UUID.randomUUID())
                .fullName("Full Name")
                .email("example1@email.com")
                .phone("1234567891")
                .streetNumber("234")
                .streetName("Street St")
                .city("City")
                .state("State")
                .country("Country")
                .postalCode("124ABC")
                .totalPrice(BigDecimal.valueOf(666.66))
                .itemCount(3)
                .status(OrderEntity.OrderStatus.PROCESSING)
                .build();
        oe.addItems(List.of(oie1, oie2, oie3));
        orderRepo.save(oe);

        OrderConfirmationMessage ocm = OrderConfirmationMessage.builder()
                .orderId(oe.getId().toString())
                .paymentId(UUID.randomUUID().toString())
                .paidAt(OffsetDateTime.now()).build();
        Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(ocm))
                .setHeader("__TypeId__", OrderConfirmationMessage.class.getSimpleName())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON).build();

        rabbitTemplate.send(RabbitMqConstants.cart_checkout_order_confirmOrder_routingKey, message);
        await().atMost(10, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> orderRepo.findById(UUID.fromString(ocm.getOrderId())).orElseThrow()
                        .getStatus() == OrderEntity.OrderStatus.CONFIRMED);

        oe = orderRepo.findById(UUID.fromString(ocm.getOrderId())).orElseThrow();
        assertNotNull(oe.getPaidAt());
        assertEquals(ocm.getPaymentId(), oe.getPaymentId().toString());
    }

    @Test
    void consumeOrderCancellationMessage_savesInDb() throws Exception {
        OrderItemEntity oie1 = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(111.11))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(111.11))
                .build();
        OrderItemEntity oie2 = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(222.22))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        OrderItemEntity oie3 = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .productName("Product 3")
                .productPrice(BigDecimal.valueOf(333.33))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(333.33))
                .build();
        OrderEntity oe = OrderEntity.builder()
                .userId(UUID.randomUUID())
                .fullName("Full Name")
                .email("example1@email.com")
                .phone("1234567891")
                .streetNumber("234")
                .streetName("Street St")
                .city("City")
                .state("State")
                .country("Country")
                .postalCode("124ABC")
                .totalPrice(BigDecimal.valueOf(666.66))
                .itemCount(3)
                .status(OrderEntity.OrderStatus.PROCESSING)
                .build();
        oe.addItems(List.of(oie1, oie2, oie3));
        orderRepo.save(oe);

        OrderCancellationMessage ocm = new OrderCancellationMessage(oe.getId().toString());
        Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(ocm))
                .setHeader("__TypeId__", OrderCancellationMessage.class.getSimpleName())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON).build();

        rabbitTemplate.send(RabbitMqConstants.cart_checkout_order_cancelOrder_routingKey, message);
        await().atMost(10, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> orderRepo.findById(UUID.fromString(ocm.getOrderId())).orElseThrow()
                        .getStatus() == OrderEntity.OrderStatus.CANCELLED);

        oe = orderRepo.findById(UUID.fromString(ocm.getOrderId())).orElseThrow();
        assertNull(oe.getPaidAt());
        assertNull(oe.getPaymentId());
    }

    @Test
    void consumeOrderConfirmationMessage_sendsCorrectProductStockUpdateMessage() throws Exception {
        OrderItemEntity oie1 = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(111.11))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(111.11))
                .build();
        OrderItemEntity oie2 = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(222.22))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        OrderItemEntity oie3 = OrderItemEntity.builder()
                .productId(UUID.randomUUID())
                .productName("Product 3")
                .productPrice(BigDecimal.valueOf(333.33))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(333.33))
                .build();
        OrderEntity oe = OrderEntity.builder()
                .userId(UUID.randomUUID())
                .fullName("Full Name")
                .email("example1@email.com")
                .phone("1234567891")
                .streetNumber("234")
                .streetName("Street St")
                .city("City")
                .state("State")
                .country("Country")
                .postalCode("124ABC")
                .totalPrice(BigDecimal.valueOf(666.66))
                .itemCount(3)
                .status(OrderEntity.OrderStatus.PROCESSING)
                .build();
        oe.addItems(List.of(oie1, oie2, oie3));
        orderRepo.save(oe);

        OrderConfirmationMessage ocm = OrderConfirmationMessage.builder()
                .orderId(oe.getId().toString())
                .paymentId(UUID.randomUUID().toString())
                .paidAt(OffsetDateTime.now()).build();

        Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(ocm))
                .setHeader("__TypeId__", OrderConfirmationMessage.class.getSimpleName())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON).build();
        rabbitTemplate.send(RabbitMqConstants.cart_checkout_order_confirmOrder_routingKey, message);
        await().atMost(10, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> orderRepo.findById(UUID.fromString(ocm.getOrderId())).orElseThrow()
                        .getStatus() == OrderEntity.OrderStatus.CONFIRMED);

        ProductStockUpdateMessage messageSentByService = rabbitTemplate.receiveAndConvert(RabbitMqConstants.product_updateStock_queue,
                10_000,
                new ParameterizedTypeReference<ProductStockUpdateMessage>() {});

        Map<String, Integer> expectedResults = Stream.of(oie1, oie2, oie3).collect(
                Collectors.toMap(o -> o.getProductId().toString(),
                        OrderItemEntity::getQuantity));

        expectedResults.forEach((k, v) -> {
            int sentValue = Optional.ofNullable(messageSentByService.getProductIdStockMap().get(k)).orElseThrow();
            assertEquals(v, sentValue);
        });
    }
}
