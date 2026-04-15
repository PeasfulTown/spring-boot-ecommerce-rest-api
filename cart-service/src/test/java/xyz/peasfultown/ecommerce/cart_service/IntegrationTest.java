package xyz.peasfultown.ecommerce.cart_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import xyz.peasfultown.ecommerce.cart_api.model.*;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;
import xyz.peasfultown.ecommerce.cart_service.repository.CartItemRepository;
import xyz.peasfultown.ecommerce.cart_service.repository.CartRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
                baseUrlProperties = "product-service.url",
                portProperties = "product-service.port"
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

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private CartItemRepository ciRepo;

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
    }

    private void stub_getProductById_returns200(Product prod) throws Exception {
        productService.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .get("/api/v1/products/" + prod.getId())
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
        UUID userId = UUID.randomUUID();
        CartEntity cartEntity = CartEntity.builder()
                .userId(userId)
                .build();
        cartEntity = cartRepo.save(cartEntity);
        CartItemEntity ci1 = CartItemEntity.builder()
                .cart(cartEntity)
                .productId(UUID.fromString(p1.getId()))
                .productName(p1.getName())
                .productPrice(p1.getPrice())
                .quantity(2)
                .subtotal(p1.getPrice().multiply(BigDecimal.valueOf(2)))
                .build();
        CartItemEntity ci2 = CartItemEntity.builder()
                .cart(cartEntity)
                .productId(UUID.fromString(p2.getId()))
                .productName(p2.getName())
                .productPrice(p2.getPrice())
                .quantity(1)
                .subtotal(p1.getPrice().multiply(BigDecimal.valueOf(1)))
                .build();
        CartItemEntity ci3 = CartItemEntity.builder()
                .cart(cartEntity)
                .productId(UUID.fromString(p3.getId()))
                .productName(p3.getName())
                .productPrice(p3.getPrice())
                .quantity(3)
                .subtotal(p1.getPrice().multiply(BigDecimal.valueOf(3)))
                .build();
        cartEntity.getItems().addAll(List.of(ci1, ci2, ci3));
        ciRepo.saveAll(List.of(ci1, ci2, ci3));
        cartRepo.save(cartEntity);

        stub_getProductsByIds_returns200(List.of(p1, p2, p3));
        String json = mvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Cart cart = objMapper.readValue(json, Cart.class);
        assertEquals(6, cart.getTotalItems());
        assertEquals(BigDecimal.valueOf(1444.43), cart.getTotalPrice());
        assertEquals(BigDecimal.valueOf(222.22), cart.getItems().get(0).getSubtotal());

        p1.setPrice(BigDecimal.valueOf(100.99));
        p2.setPrice(BigDecimal.valueOf(200.99));
        p3.setPrice(BigDecimal.valueOf(300.99));
        stub_getProductsByIds_returns200(List.of(p1, p2, p3));

        json = mvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        cart = objMapper.readValue(json, Cart.class);
        assertEquals(6, cart.getTotalItems());
        assertEquals(BigDecimal.valueOf(1305.94), cart.getTotalPrice());
        assertEquals(BigDecimal.valueOf(201.98), cart.getItems().get(0).getSubtotal());
    }

    @Test
    void addItemToCart_returns200_whenValidInput() throws Exception {
        UUID userId = UUID.randomUUID();

        stub_getProductById_returns200(p1);
        AddItemReq req = new AddItemReq()
                .productId(p1.getId())
                .quantity(2);

        mvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
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
        UUID userId = UUID.randomUUID();

        CartEntity cartEntity = new CartEntity();
        cartEntity.setUserId(userId);
        cartEntity = cartRepo.save(cartEntity);

        CartItemEntity ci1 = CartItemEntity.builder()
                .cart(cartEntity)
                .productId(UUID.fromString(p1.getId()))
                .productName(p1.getName())
                .productPrice(p1.getPrice())
                .quantity(1)
                .subtotal(p1.getPrice())
                .build();
        CartItemEntity ci2 = CartItemEntity.builder()
                .cart(cartEntity)
                .productId(UUID.fromString(p2.getId()))
                .productName(p2.getName())
                .productPrice(p2.getPrice())
                .quantity(2)
                .subtotal(p2.getPrice().multiply(BigDecimal.valueOf(2)))
                .build();
        CartItemEntity ci3 = CartItemEntity.builder()
                .cart(cartEntity)
                .productId(UUID.fromString(p3.getId()))
                .productName(p3.getName())
                .productPrice(p3.getPrice())
                .quantity(3)
                .subtotal(p3.getPrice().multiply(BigDecimal.valueOf(3)))
                .build();

        cartEntity.getItems().addAll(List.of(ci1, ci2, ci3));

        cartEntity = cartRepo.findCartByUserId(userId).get();
        assertThat(cartEntity.getItems()).hasSize(3);

        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p1.getId()))).isPresent();
        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p2.getId()))).isPresent();
        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p3.getId()))).isPresent();

        mvc.perform(delete("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent());

        cartEntity = cartRepo.findCartByUserId(userId).get();
        assertThat(cartEntity.getItems()).hasSize(0);

        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p1.getId()))).isNotPresent();
        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p2.getId()))).isNotPresent();
        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p3.getId()))).isNotPresent();
    }

}
