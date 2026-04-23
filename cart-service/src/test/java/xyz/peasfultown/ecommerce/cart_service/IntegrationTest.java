package xyz.peasfultown.ecommerce.cart_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import xyz.peasfultown.ecommerce.cart_api.model.*;
import xyz.peasfultown.ecommerce.cart_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.cart_service.dto.Address;
import xyz.peasfultown.ecommerce.cart_service.dto.OrderSubmission;
import xyz.peasfultown.ecommerce.cart_service.dto.User;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;
import xyz.peasfultown.ecommerce.cart_service.mapper.CartItemMapper;
import xyz.peasfultown.ecommerce.cart_service.mapper.CartMapper;
import xyz.peasfultown.ecommerce.cart_service.repository.CartItemRepository;
import xyz.peasfultown.ecommerce.cart_service.repository.CartRepository;
import xyz.peasfultown.ecommerce.cart_service.service.CartService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
@EnableWireMock({
        @ConfigureWireMock(
                name = "product-service",
                baseUrlProperties = "PRODUCT_SERVICE_URL",
                portProperties = "product-service.port"
        ),
        @ConfigureWireMock(
                name = "user-service",
                baseUrlProperties = "USER_SERVICE_URL",
                portProperties = "user-service.port"
        )
})
@Slf4j
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

    @InjectWireMock("user-service")
    private WireMockServer userService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private CartItemRepository ciRepo;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper ciMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static RabbitAdmin rabbitAdmin;

    @Autowired
    private void setRabbitAdmin(RabbitAdmin rabbitAdmin) {
        IntegrationTest.rabbitAdmin = rabbitAdmin;
    }

    private Product p1;
    private Product p2;
    private Product p3;

    private User user1;
    private Address address1;
    private CartItemEntity ci1;
    private CartItemEntity ci2;
    private CartItemEntity ci3;
    private CartEntity ce1;

    private User user2;
    private Address address2;
    private CartItemEntity ci4;
    private CartItemEntity ci5;
    private CartEntity ce2;

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
                .stockStatus(ProductStockStatus.IN_STOCK)
                .activeStatus(ProductActiveStatus.ACTIVE);
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
                .stockStatus(ProductStockStatus.IN_STOCK)
                .activeStatus(ProductActiveStatus.ACTIVE);

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
                .stockStatus(ProductStockStatus.IN_STOCK)
                .activeStatus(ProductActiveStatus.ACTIVE);

        user1 = User.builder()
                .id(UUID.randomUUID().toString())
                .email("user1@example.com")
                .phone("1234567890")
                .build();

        user2 = User.builder()
                .id(UUID.randomUUID().toString())
                .email("user2@example.com")
                .phone("1234567891")
                .build();

        address1 = Address.builder()
                .id(UUID.randomUUID().toString())
                .firstName("Firsta")
                .lastName("Lasta")
                .number("123")
                .street("Streeta St")
                .city("Citya")
                .state("Statea")
                .country("Countrya")
                .postalCode("111AAA")
                .build();

        address2 = Address.builder()
                .id(UUID.randomUUID().toString())
                .firstName("Firstb")
                .lastName("Lastb")
                .number("234")
                .street("Streetb St")
                .city("Cityb")
                .state("Stateb")
                .country("Countryb")
                .postalCode("222BBB")
                .build();

        ce1 = CartEntity.builder()
                .userId(UUID.fromString(user1.getId()))
                .build();
        ce1 = cartRepo.save(ce1);
        ci1 = CartItemEntity.builder()
                .cart(ce1)
                .productId(UUID.fromString(p1.getId()))
                .productName(p1.getName())
                .productPrice(p1.getPrice())
                .quantity(1)
                .subtotal(BigDecimal.valueOf(111.11))
                .build();
        ci2 = CartItemEntity.builder()
                .cart(ce1)
                .productId(UUID.fromString(p2.getId()))
                .productName(p2.getName())
                .productPrice(p2.getPrice())
                .quantity(2)
                .subtotal(BigDecimal.valueOf(444.44))
                .build();
        ci3 = CartItemEntity.builder()
                .cart(ce1)
                .productId(UUID.fromString(p3.getId()))
                .productName(p3.getName())
                .productPrice(p3.getPrice())
                .quantity(1)
                .subtotal(BigDecimal.valueOf(333.33))
                .build();
        ce1.getItems().addAll(List.of(ci1, ci2, ci3));
        ce1.setTotalItems(4);
        ce1.setTotalPrice(BigDecimal.valueOf(888.88));
        ce1 = cartRepo.save(ce1);

        ce2 = CartEntity.builder()
                .userId(UUID.fromString(user2.getId()))
                .build();
        ce2 = cartRepo.save(ce2);
        ci4 = CartItemEntity.builder()
                .cart(ce2)
                .productId(UUID.fromString(p1.getId()))
                .productName(p1.getName())
                .productPrice(p1.getPrice())
                .quantity(1)
                .subtotal(BigDecimal.valueOf(111.11))
                .build();
        ci5 = CartItemEntity.builder()
                .cart(ce2)
                .productId(UUID.fromString(p2.getId()))
                .productName(p2.getName())
                .productPrice(p2.getPrice())
                .quantity(1)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        ce2.getItems().addAll(List.of(ci4, ci5));
        ce2.setTotalItems(2);
        ce2.setTotalPrice(BigDecimal.valueOf(333.33));
        cartRepo.save(ce2);
    }

    @AfterEach
    void tear() {
        rabbitAdmin.purgeQueue(RabbitMqConstants.orderSubmitted_queue);
    }

    @AfterAll
    static void teardown() {
        rabbitAdmin.deleteExchange(RabbitMqConstants.exchange);
    }


    private void stub_getProductById_returns200(Product prod) throws Exception {
        productService.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .get(urlPathTemplate("/api/v1/products/{id}"))
                .withPathParam("id", equalTo(prod.getId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objMapper.writeValueAsString(prod))));
    }

    private void stub_getProductById_returns404(String productId) throws Exception {
        productService.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .get(urlPathTemplate("/api/v1/products/{id}"))
                .withPathParam("id", equalTo(productId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")));
    }

    private void stub_getProductsByIds_returns200(List<Product> products) throws Exception {
        List<ProductId> productIds = products.stream()
                .map(p -> new ProductId().id(p.getId())).toList();
        productService.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .post("/api/v1/products/batch")
                .withRequestBody(equalToJson(objMapper.writeValueAsString(productIds)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objMapper.writeValueAsString(products))));

    }

    private void stub_getUserById_returns200(User user) throws Exception {
        userService.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .get(urlPathTemplate("/api/v1/users/{id}"))
                .withPathParam("id", equalTo(user.getId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objMapper.writeValueAsString(user))));
    }

    private void stub_getAddressById_returns200(Address address) throws Exception {
        userService.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .get(urlPathTemplate("/api/v1/addresses/{id}"))
                .withPathParam("id", equalTo(address.getId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objMapper.writeValueAsString(address))));
    }

    @Test
    void getMyCart_createsNewCartForUser_whenCartDoesntAlreadyExists() throws Exception {
        UUID userId = UUID.randomUUID();
        mvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());

        assertThat(cartRepo.findCartByUserId(userId)).isPresent();
    }

    @Test
    void getMyCart_returnsUpdatedCartItems_whenProductIsUpdated() throws Exception {
        UUID userId = UUID.randomUUID();

        AddItemReq i1 = new AddItemReq()
                .productId(p1.getId())
                .quantity(1);

        AddItemReq i2 = new AddItemReq()
                .productId(p2.getId())
                .quantity(2);

        // add first item
        stub_getProductById_returns200(p1);
        mvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(i1))
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value(p1.getName()));

        // add second item
        stub_getProductById_returns200(p2);
        stub_getProductsByIds_returns200(List.of(p1));
        mvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(i2))
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value(p2.getName()));

        // check cart
        List<Product> products = List.of(p1, p2);
        List<ProductId> productIds = products.stream().map(p -> new ProductId().id(p.getId())).toList();
        stub_getProductsByIds_returns200(products);
        MvcResult result = mvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Cart cart = objMapper.readValue(json, Cart.class);

        // product service updates the products and will return new values when requested
        p1.setName("New product name");
        p2.setPrice(BigDecimal.valueOf(123.99));
        stub_getProductsByIds_returns200(products);

        // check cart after product service updated products information
        result = mvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        json = result.getResponse().getContentAsString();
        cart = objMapper.readValue(json, Cart.class);
        CartItem ci1 = cart.getItems().stream().filter(c -> c.getProductId().equals(p1.getId())).findFirst().get();
        CartItem ci2 = cart.getItems().stream().filter(c -> c.getProductId().equals(p2.getId())).findFirst().get();
        assertEquals("New product name", ci1.getProductName());
        assertEquals(BigDecimal.valueOf(123.99), ci2.getProductPrice());
    }

    @Test
    void getMyCart_pricesAreSavedCorrectly_afterProductUpdate() throws Exception {
        stub_getProductsByIds_returns200(List.of(p1, p2, p3));
        String json = mvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", user1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Cart cart = objMapper.readValue(json, Cart.class);
        assertEquals(BigDecimal.valueOf(888.88), cart.getTotalPrice());
        assertEquals(3, cart.getItems().size());

        p1.setPrice(BigDecimal.valueOf(100.99));
        p2.setPrice(BigDecimal.valueOf(200.99));
        p3.setPrice(BigDecimal.valueOf(300.99));
        stub_getProductsByIds_returns200(List.of(p1, p2, p3));

        json = mvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", user1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        cart = objMapper.readValue(json, Cart.class);
        assertEquals(BigDecimal.valueOf(803.96), cart.getTotalPrice());
    }

    @Test
    void addItemToCart_savesToDatabase() throws Exception {
        UUID userId = UUID.randomUUID();

        stub_getProductById_returns200(p1);
        AddItemReq r1 = new AddItemReq()
                .productId(p1.getId())
                .quantity(2);

        AddItemReq r2 = new AddItemReq()
                .productId(p2.getId())
                .quantity(2);

        stub_getProductById_returns200(p1);
        mvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(r1)))
                .andExpect(status().isOk());

        stub_getProductById_returns200(p2);
        stub_getProductsByIds_returns200(List.of(p1));
        mvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(r2)))
                .andExpect(status().isOk());

        CartEntity ce = cartRepo.findCartByUserId(userId).get();

        assertThat(ciRepo.findCartItemByCartIdAndProductId(ce.getId(), UUID.fromString(p1.getId()))).isPresent();
        assertThat(ciRepo.findCartItemByCartIdAndProductId(ce.getId(), UUID.fromString(p2.getId()))).isPresent();
    }

    @Test
    void addItemToCart_savesCorrectPricesAndQuantity() throws Exception {
        UUID userId = UUID.randomUUID();

        stub_getProductById_returns200(p1);
        AddItemReq r1 = new AddItemReq()
                .productId(p1.getId())
                .quantity(2);

        AddItemReq r2 = new AddItemReq()
                .productId(p2.getId())
                .quantity(2);

        stub_getProductById_returns200(p1);
        mvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(r1)))
                .andExpect(status().isOk());

        stub_getProductById_returns200(p2);
        stub_getProductsByIds_returns200(List.of(p1));
        mvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(r2)))
                .andExpect(status().isOk());

        CartEntity ce = cartRepo.findCartByUserId(userId).get();
        assertEquals(BigDecimal.valueOf(666.66), ce.getTotalPrice());
        assertEquals(4, ce.getTotalItems());
        CartItemEntity cie1 = ciRepo.findCartItemByCartIdAndProductId(ce.getId(), UUID.fromString(p1.getId())).get();
        assertEquals(BigDecimal.valueOf(222.22), cie1.getSubtotal());
        CartItemEntity cie2 = ciRepo.findCartItemByCartIdAndProductId(ce.getId(), UUID.fromString(p2.getId())).get();
        assertEquals(BigDecimal.valueOf(444.44), cie2.getSubtotal());
    }

    @Test
    void addItemToCart_showsUpInCorrectUserCart() throws Exception {
        UUID userId = UUID.randomUUID();

        AddItemReq req = new AddItemReq()
                .productId(p1.getId())
                .quantity(1);

        stub_getProductById_returns200(p1);
        mvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req))
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());

        UUID cartId = cartRepo.findCartByUserId(userId).orElseThrow().getId();

        stub_getProductsByIds_returns200(List.of(p1));
        mvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.items").isNotEmpty());

        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartId, UUID.fromString(p1.getId()))).isPresent();
    }

    @Test
    void addItemToCart_returns400_whenProductIsOutOfStock() throws Exception {
        UUID userId = UUID.randomUUID();

        AddItemReq req = new AddItemReq()
                .productId(p1.getId())
                .quantity(2);

        p1.setStockStatus(ProductStockStatus.OUT_OF_STOCK);
        stub_getProductById_returns200(p1);
        mvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req))
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemToCart_returns404_whenProductNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        AddItemReq req = new AddItemReq()
                .productId(p1.getId())
                .quantity(2);

        stub_getProductById_returns404(p1.getId());
        mvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req))
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void clearMyCart_deletesAllItemsFromCart() throws Exception {
        mvc.perform(delete("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", user1.getId()))
                .andExpect(status().isNoContent());

        CartEntity cartEntity = cartRepo.findCartByUserId(UUID.fromString(user1.getId())).get();
        assertThat(cartEntity.getItems()).hasSize(0);

        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p1.getId()))).isNotPresent();
        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p2.getId()))).isNotPresent();
        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p3.getId()))).isNotPresent();
    }

    @Test
    void removeItemFromCart_removesItemFromDatabase() throws Exception {
        mvc.perform(delete("/api/v1/cart/items/{id}", ce1.getItems().get(0).getId().toString())
                        .header("X-User-Id", user1.getId()))
                .andExpect(status().isNoContent());

        assertThat(ciRepo.findCartItemByCartIdAndProductId(ce1.getId(), UUID.fromString(p1.getId()))).isNotPresent();
    }

    @Test
    void updateItemQuantity_savesCorrectValuesInDatabase() throws Exception {
        UpdateItemQuantityReq req = new UpdateItemQuantityReq()
                .quantity(4);

        String json = mvc.perform(patch("/api/v1/cart/items/{id}", ce1.getItems().get(0).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req))
                        .header("X-User-Id", user1.getId().toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CartItem resultObj = objMapper.readValue(json, CartItem.class);
        assertEquals(4, resultObj.getQuantity());
        assertEquals(BigDecimal.valueOf(444.44), resultObj.getSubtotal());

        CartEntity cartEntity = cartRepo.findCartByUserId(UUID.fromString(user1.getId())).get();
        assertEquals(BigDecimal.valueOf(1222.21), cartEntity.getTotalPrice());
        assertEquals(7, cartEntity.getTotalItems());
    }

    @Test
    void checkoutCart_sendsMessage() throws Exception {
        CartCheckoutReq req = CartCheckoutReq.builder()
                .addressId(address1.getId())
                // TODO: add card id
                .cardId(UUID.randomUUID().toString())
                .build();

        stub_getUserById_returns200(user1);
        stub_getAddressById_returns200(address1);
        stub_getProductsByIds_returns200(List.of(p1, p2, p3));
        OrderSubmission expected = new OrderSubmission(user1, address1, cartMapper.toModel(ce1));
        mvc.perform(post("/api/v1/cart/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req))
                        .header("X-User-Id", user1.getId()))
                .andExpect(status().isAccepted());

        OrderSubmission result = rabbitTemplate.receiveAndConvert(RabbitMqConstants.orderSubmitted_queue,
                10_000, new ParameterizedTypeReference<OrderSubmission>() {
                });
        assertThat(expected.getItems()).isNotEmpty();
        assertEquals(expected.getUserId(), result.getUserId());
        assertEquals(expected.getContactEmail(), result.getContactEmail());
        assertEquals(expected.getContactPhone(), result.getContactPhone());
        assertEquals(expected.getAddressNumber(), result.getAddressNumber());
        assertEquals(expected.getAddressStreet(), result.getAddressStreet());
        assertEquals(expected.getAddressState(), result.getAddressState());
        assertEquals(expected.getAddressCountry(), result.getAddressCountry());
        assertEquals(expected.getAddressPostalCode(), result.getAddressPostalCode());
        assertEquals(expected.getOrderTotal(), result.getOrderTotal());
        assertEquals(expected.getOrderItemCount(), result.getOrderItemCount());
        assertEquals(expected.getItems(), result.getItems());
    }

    @Test
    void checkoutCart_returns400_whenCartIsEmpty() throws Exception {
        UUID userId = UUID.randomUUID();
        CartCheckoutReq req = CartCheckoutReq.builder()
                .addressId(address1.getId())
                .cardId(UUID.randomUUID().toString())
                .build();

        mvc.perform(post("/api/v1/cart/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objMapper.writeValueAsString(req))
                .header("X-User-Id", userId.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutCart_clearsCart() throws Exception {
        CartCheckoutReq req = CartCheckoutReq.builder()
                .addressId(address1.getId())
                .cardId(UUID.randomUUID().toString())
                .build();

        stub_getProductsByIds_returns200(List.of(p1, p2, p3));
        stub_getUserById_returns200(user1);
        stub_getAddressById_returns200(address1);
        mvc.perform(post("/api/v1/cart/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req))
                        .header("X-User-Id", user1.getId()))
                .andExpect(status().isAccepted());

        assertThat(cartRepo.findCartByUserId(UUID.fromString(user1.getId())).get().getItems()).isEmpty();
        assertThat(ciRepo.findCartItemsByCartId(ce1.getId())).isEmpty();
    }
}
