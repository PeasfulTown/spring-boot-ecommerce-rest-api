package xyz.peasfultown.ecommerce.cart_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import org.wiremock.spring.InjectWireMock;
import xyz.peasfultown.ecommerce.cart_api.model.*;
import xyz.peasfultown.ecommerce.cart_service.dto.BatchProductIdRequest;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;
import xyz.peasfultown.ecommerce.cart_service.mapper.CartItemMapper;
import xyz.peasfultown.ecommerce.cart_service.mapper.CartMapper;
import xyz.peasfultown.ecommerce.cart_service.repository.CartItemRepository;
import xyz.peasfultown.ecommerce.cart_service.repository.CartRepository;
import xyz.peasfultown.ecommerce.cart_service.service.CartService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@EnableWireMock({
        @ConfigureWireMock(
                name = "product-service",
                baseUrlProperties = "PRODUCT_SERVICE_URL",
                portProperties = "product-service.port"
        )
})
@Slf4j
public class IntegrationTest {
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
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper ciMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private UUID userId;

    private Product p1;
    private Product p2;
    private Product p3;

    private CartItemEntity ci1;
    private CartItemEntity ci2;
    private CartItemEntity ci3;
    private CartEntity ce1;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();

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

        ci1 = CartItemEntity.builder()
                .productId(UUID.fromString(p1.getId()))
                .quantity(1)
                .build();
        ci2 = CartItemEntity.builder()
                .productId(UUID.fromString(p2.getId()))
                .quantity(2)
                .build();
        ci3 = CartItemEntity.builder()
                .productId(UUID.fromString(p3.getId()))
                .quantity(1)
                .build();
        ce1 = CartEntity.builder()
                .userId(userId)
                .build();
        ce1.addItems(List.of(ci1, ci2, ci3));
        ce1 = cartRepo.save(ce1);
    }

    private void stub_getProductById_returns200(Product prod) throws Exception {
        productService.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .get(urlPathTemplate("/api/v1/products/{id}"))
                .withPathParam("id", equalTo(prod.getId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(oMapper.writeValueAsString(prod))));
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
        List<String> productIds = products.stream()
                .map(p -> p.getId()).toList();
        BatchProductIdRequest req = new BatchProductIdRequest(productIds);
        productService.stubFor(com.github.tomakehurst.wiremock.client.WireMock
                .post("/api/v1/products/batch")
                .withRequestBody(equalToJson(oMapper.writeValueAsString(req)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(oMapper.writeValueAsString(products))));
    }

    void testCartItemsProductsEqual(Cart cart, List<Product> products) {
        Map<String, Product> productMap = products.stream().collect(Collectors.toMap(
                Product::getId, Function.identity()));
        cart.getItems().forEach(i -> {
            Product p = Optional.ofNullable(productMap.get(i.getProductId()))
                    .orElseThrow();
            assertEquals(i.getProductName(), p.getName());
            assertEquals(i.getProductPrice(), p.getPrice());
        });
    }

    @Test
    void getCart_createsNewCartForUser_whenCartDoesntAlreadyExists() throws Exception {
        UUID userId = UUID.randomUUID();
        mvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());

        assertThat(cartRepo.findCartByUserId(userId)).isPresent();
    }

    @Test
    void getCart_returnsUpdatedCartItems_whenProductIsUpdated() throws Exception {
        // product service updates the products and will
        // return new values when those products are requested
        p1.setName("New product name");
        p1.setPrice(BigDecimal.valueOf(12.99));
        p2.setName("Another new name");
        p2.setPrice(BigDecimal.valueOf(123.99));
        p3.setPrice(BigDecimal.valueOf(234.99));

        List<Product> products = List.of(p1, p2, p3);
        stub_getProductsByIds_returns200(products);

        // check cart after product service updated products information
        String json = mvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Cart cart = oMapper.readValue(json, Cart.class);

        testCartItemsProductsEqual(cart, products);
        assertEquals(BigDecimal.valueOf(495.96), cart.getTotalPrice());
    }

    @Test
    void getCart_returnsCorrectItems_whenAProductGoesOutOfStock() throws Exception {
        fail();
    }

    @Test
    void createCartItem_savesToDatabase() throws Exception {
        UUID userId = UUID.randomUUID();

        ItemCreateRequest req1 = new ItemCreateRequest()
                .productId(p1.getId())
                .quantity(2);

        ItemCreateRequest req2 = new ItemCreateRequest()
                .productId(p2.getId())
                .quantity(2);

        stub_getProductById_returns200(p1);
        mvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(p1.getId()))
                .andExpect(jsonPath("$.productName").value(p1.getName()))
                .andExpect(jsonPath("$.productPrice").value(p1.getPrice()))
                .andExpect(jsonPath("$.quantity").value(req1.getQuantity()))
                .andExpect(jsonPath("$.subtotal").value(p1.getPrice().multiply(BigDecimal.valueOf(req1.getQuantity()))))
                ;

        stub_getProductById_returns200(p2);
        mvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(p2.getId()))
                .andExpect(jsonPath("$.productName").value(p2.getName()))
                .andExpect(jsonPath("$.productPrice").value(p2.getPrice()))
                .andExpect(jsonPath("$.quantity").value(req1.getQuantity()))
                .andExpect(jsonPath("$.subtotal").value(p2.getPrice().multiply(BigDecimal.valueOf(req1.getQuantity()))))
                ;

        CartEntity ce = cartRepo.findCartByUserId(userId).get();
        assertThat(ciRepo.findCartItemByCartIdAndProductId(ce.getId(), UUID.fromString(p1.getId()))).isPresent();
        assertThat(ciRepo.findCartItemByCartIdAndProductId(ce.getId(), UUID.fromString(p2.getId()))).isPresent();
    }


    @Test
    void createCartItem_returns400_whenProductIsOutOfStock() throws Exception {
        UUID userId = UUID.randomUUID();

        ItemCreateRequest req = new ItemCreateRequest()
                .productId(p1.getId())
                .quantity(2);

        p1.setStockStatus(StockStatus.OUT_OF_STOCK);
        stub_getProductById_returns200(p1);
        mvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isBadRequest());
        CartEntity ce = cartRepo.findCartByUserId(userId).orElseThrow();
        assertThat(ce.getItems()).isEmpty();
    }

    @Test
    void createCartItem_returns404_whenProductNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        ItemCreateRequest req = new ItemCreateRequest()
                .productId(p1.getId())
                .quantity(2);

        stub_getProductById_returns404(p1.getId());
        mvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void clearCart_deletesAllItemsFromCart() throws Exception {
        mvc.perform(delete("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent());

        CartEntity cartEntity = cartRepo.findCartByUserId(userId).orElseThrow();
        assertThat(cartEntity.getItems()).hasSize(0);

        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p1.getId()))).isNotPresent();
        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p2.getId()))).isNotPresent();
        assertThat(ciRepo.findCartItemByCartIdAndProductId(cartEntity.getId(), UUID.fromString(p3.getId()))).isNotPresent();
    }

    @Test
    void removeCartItem_removesItemFromDatabase() throws Exception {
        mvc.perform(delete("/api/v1/cart/items/{id}", ce1.getItems().get(0).getId().toString())
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent());

        assertThat(ciRepo.findCartItemByCartIdAndProductId(ce1.getId(), UUID.fromString(p1.getId()))).isNotPresent();
    }

    @Test
    void checkoutCart_returns400_whenCartIsEmpty() throws Exception {
        ce1.getItems().clear();
        cartRepo.save(ce1);
        CartCheckoutRequest req = CartCheckoutRequest.builder()
                .addressId(UUID.randomUUID().toString())
                .cardId(UUID.randomUUID().toString())
                .build();

        mvc.perform(post("/api/v1/cart/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(oMapper.writeValueAsString(req))
                .header("X-User-Id", userId.toString()))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void checkoutCart_clearsCart() throws Exception {
        CartCheckoutRequest req = CartCheckoutRequest.builder()
                .addressId(UUID.randomUUID().toString())
                .cardId(UUID.randomUUID().toString())
                .build();

        stub_getProductsByIds_returns200(List.of(p1, p2, p3));
        mvc.perform(post("/api/v1/cart/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isAccepted());

        assertThat(cartRepo.findCartByUserId(userId).orElseThrow().getItems()).isEmpty();
        assertThat(ciRepo.findCartItemsByCartId(ce1.getId())).isEmpty();
    }
}
