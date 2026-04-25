package xyz.peasfultown.ecommerce.cart_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import xyz.peasfultown.ecommerce.cart_api.model.*;
import xyz.peasfultown.ecommerce.cart_service.client.ProductServiceClient;
import xyz.peasfultown.ecommerce.cart_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.cart_service.dto.BatchProductIdRequest;
import xyz.peasfultown.ecommerce.cart_service.dto.OrderCreateMessage;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;
import xyz.peasfultown.ecommerce.cart_service.repository.CartItemRepository;
import xyz.peasfultown.ecommerce.cart_service.repository.CartRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@EnableWireMock(
        @ConfigureWireMock(
                name = "product-service",
                baseUrlProperties = "PRODUCT_SERVICE_URL",
                portProperties = "product-service.port"
        )
)
@SpringBootTest
@ActiveProfiles({"test", "rabbitmq"})
@AutoConfigureMockMvc
@Slf4j
public class AsyncServiceFunctionTest {
    @InjectWireMock("product-service")
    private WireMockServer productService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper oMapper;

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private CartItemRepository ciRepo;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Product p1;
    private Product p2;
    private Product p3;

    @BeforeEach
    void setup() {
        p1 = new Product()
                .id(UUID.randomUUID().toString())
                .name("Product 1")
                .description("Original product 1")
                .price(BigDecimal.valueOf(111.11))
                .imageUrls(List.of(
                        "http://wwww.images.com/products/product1_1.jpg",
                        "http://wwww.images.com/products/product1_2.jpg",
                        "http://wwww.images.com/products/product1_3.jpg"
                ))
                .stockStatus(StockStatus.IN_STOCK)
                .activeStatus(ActiveStatus.ACTIVE);

        p2 = new Product()
                .id(UUID.randomUUID().toString())
                .name("Product 2")
                .description("Original product 2")
                .price(BigDecimal.valueOf(222.22))
                .imageUrls(List.of(
                        "http://wwww.images.com/products/product2_1.jpg",
                        "http://wwww.images.com/products/product2_2.jpg",
                        "http://wwww.images.com/products/product2_3.jpg"
                ))
                .stockStatus(StockStatus.IN_STOCK)
                .activeStatus(ActiveStatus.ACTIVE);

        p3 = new Product()
                .id(UUID.randomUUID().toString())
                .name("Product 3")
                .description("Original product 3")
                .price(BigDecimal.valueOf(333.33))
                .imageUrls(List.of(
                        "http://wwww.images.com/products/product3_1.jpg",
                        "http://wwww.images.com/products/product3_2.jpg",
                        "http://wwww.images.com/products/product3_3.jpg"
                ))
                .stockStatus(StockStatus.IN_STOCK)
                .activeStatus(ActiveStatus.ACTIVE);

    }

    @AfterEach
    void teardown() {
        cartRepo.deleteAll();
        assertThat(ciRepo.findAll()).isEmpty();
    }

    void stub_getProductsByIds(List<Product> products) throws Exception {
        List<String> productIds = products.stream()
                .map(p -> p.getId()).toList();
        BatchProductIdRequest req = new BatchProductIdRequest(productIds);
        productService.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .post("/api/v1/products/batch")
                .withRequestBody(equalToJson(oMapper.writeValueAsString(req), true, false))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(oMapper.writeValueAsString(products))));
    }

    @Test
    void cartCheckout_sendsOrderCreateMessage() throws Exception {
        UUID userId = UUID.randomUUID();
        CartItemEntity ci1 = CartItemEntity.builder()
                .productId(UUID.fromString(p1.getId()))
                .quantity(1)
                .build();
        CartItemEntity ci2 = CartItemEntity.builder()
                .productId(UUID.fromString(p2.getId()))
                .quantity(2)
                .build();
        CartItemEntity ci3 = CartItemEntity.builder()
                .productId(UUID.fromString(p3.getId()))
                .quantity(1)
                .build();
        CartEntity ce1 = CartEntity.builder()
                .userId(userId)
                .build();
        ce1.addItems(List.of(ci1, ci2, ci3));
        cartRepo.save(ce1);
        stub_getProductsByIds(List.of(p1, p2, p3));

        CartCheckoutRequest req = CartCheckoutRequest.builder()
                .addressId(UUID.randomUUID().toString())
                .cardId(UUID.randomUUID().toString())
                .build();
        mvc.perform(post("/api/v1/cart/checkout")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req)))
                .andExpect(status().isAccepted());

        OrderCreateMessage message = rabbitTemplate.receiveAndConvert(
                RabbitMqConstants.order_createOrder_queue,
                10_000,
                new ParameterizedTypeReference<OrderCreateMessage>() {
                });

        assertEquals(userId.toString(), message.getUserId());
        assertEquals(req.getCardId(), message.getCardId());
        assertEquals(req.getAddressId(), message.getAddressId());
        assertEquals(BigDecimal.valueOf(888.88), message.getTotalPrice());
        assertEquals(4, message.getItemCount());

        Map<String, CartItem> cartItemMap = message.getItems().stream()
                .collect(Collectors.toMap(CartItem::getProductId, Function.identity()));
        Map<String, Product> productMap = List.of(p1, p2, p3).stream().collect(
                Collectors.toMap(Product::getId, Function.identity()));

        ce1.getItems().forEach(i -> {
            CartItem ci = Optional.ofNullable(cartItemMap.get(i.getProductId().toString())).orElseThrow();
            Product product = productMap.get(i.getProductId().toString());
            assertEquals(product.getName(), ci.getProductName());
            assertEquals(product.getPrice(), ci.getProductPrice());
            assertEquals(i.getQuantity(), ci.getQuantity());
            assertEquals(product.getPrice()
                    .multiply(BigDecimal.valueOf(i.getQuantity())), ci.getSubtotal());
        });
    }


}
