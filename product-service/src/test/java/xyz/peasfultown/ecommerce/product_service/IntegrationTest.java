package xyz.peasfultown.ecommerce.product_service;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import xyz.peasfultown.ecommerce.product_api.model.*;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;
import xyz.peasfultown.ecommerce.product_service.repository.CategoryRepository;
import xyz.peasfultown.ecommerce.product_service.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Slf4j
public class IntegrationTest {
    @Autowired
    private ProductRepository prodRepo;

    @Autowired
    private CategoryRepository catRepo;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper oMapper;

    private CategoryEntity c1;
    private CategoryEntity c2;
    private CategoryEntity c3;

    private ProductEntity p1;
    private ProductEntity p2;
    private ProductEntity p3;
    private ProductEntity p4;

    @BeforeEach
    void setup() {
        c1 = CategoryEntity.builder()
                .id(UUID.randomUUID())
                .name("Category 1")
                .description("Description of category 1")
                .build();

        c2 = CategoryEntity.builder()
                .id(UUID.randomUUID())
                .name("Category 2")
                .description("Description of category 2")
                .build();

        c3 = CategoryEntity.builder()
                .id(UUID.randomUUID())
                .name("Category 3")
                .description("Description of category 3")
                .build();

        catRepo.saveAll(List.of(c1, c2, c3));

        p1 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Product 1")
                .description("Description of product 1")
                .price(BigDecimal.valueOf(11.11))
                .imageUrls(List.of(
                        "http://images.com/product1_1.jpg",
                        "http://images.com/product1_2.jpg",
                        "http://images.com/product1_3.jpg"
                ))
                .activeStatus(ProductEntity.ActiveStatus.ACTIVE)
                .stock(50)
                .category(c1)
                .build();

        p2 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Product 2")
                .description("Description of product 2")
                .price(BigDecimal.valueOf(22.22))
                .imageUrls(List.of(
                        "http://images.com/product2_1.jpg",
                        "http://images.com/product2_2.jpg",
                        "http://images.com/product2_3.jpg"
                ))
                .activeStatus(ProductEntity.ActiveStatus.ACTIVE)
                .stock(50)
                .category(c1)
                .build();

        p3 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Product 3")
                .description("Description of product 3")
                .price(BigDecimal.valueOf(33.33))
                .imageUrls(List.of(
                        "http://images.com/product3_1.jpg",
                        "http://images.com/product3_2.jpg",
                        "http://images.com/product3_3.jpg"
                ))
                .activeStatus(ProductEntity.ActiveStatus.ACTIVE)
                .stock(50)
                .category(c2)
                .build();

        p4 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Product 4")
                .description("Description of product 4")
                .price(BigDecimal.valueOf(44.44))
                .imageUrls(List.of(
                        "http://images.com/product4_1.jpg",
                        "http://images.com/product4_2.jpg",
                        "http://images.com/product4_3.jpg"
                ))
                .activeStatus(ProductEntity.ActiveStatus.ACTIVE)
                .stock(50)
                .category(c2)
                .build();

        prodRepo.saveAll(List.of(p1, p2, p3, p4));
    }

    @Test
    void queryProducts_return200() throws Exception {
        mvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
        ;

        MultiValueMap<String, String> queries = new LinkedMultiValueMap<>();
        queries.add("pageNumber", "0");
        queries.add("pageSize", "2");
        mvc.perform(get("/api/v1/products")
                        .queryParams(queries))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page.totalPages").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0))
        ;
    }

    @Test
    void getProductBatchById_return200() throws Exception {
        BatchProductIdRequest req = BatchProductIdRequest.builder()
                .content(List.of(
                        p2.getId().toString(),
                        p3.getId().toString()
                ))
                .build();

        mvc.perform(post("/api/v1/products/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
        ;

        req.content(List.of());

        mvc.perform(post("/api/v1/products/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
        ;
    }

    @Test
    void getProductById_return200_whenValidRequest() throws Exception {
        mvc.perform(get("/api/v1/products/{id}", p1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(p1.getId().toString()))
        ;
    }

    @Test
    void getProductById_returns404_whenProductNotFound() throws Exception {
        mvc.perform(get("/api/v1/products/{id}", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addProduct_returns201_whenValidRequest() throws Exception {
        ProductCreateRequest req = ProductCreateRequest.builder()
                .name("New product")
                .imageUrls(List.of(
                        "http://images.com/product_new1.jpg",
                        "http://images.com/product_new2.jpg"
                ))
                .price(BigDecimal.valueOf(123.11))
                .category(c1.getName())
                .stock(50)
                .description("New product description")
                .build();
        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stockStatus").value("IN_STOCK"))
                .andExpect(jsonPath("$.stock").value(50))
        ;

        List<ProductEntity> pes = prodRepo.findProductsWithIdsNotIn(List.of(p1.getId(), p2.getId(), p3.getId(), p4.getId()));
        assertThat(pes).isNotEmpty();
        assertEquals(req.getName(), pes.get(0).getName());
        assertEquals(req.getDescription(), pes.get(0).getDescription());
        assertEquals(req.getCategory(), pes.get(0).getCategory().getName());
        assertEquals(req.getPrice(), pes.get(0).getPrice());
        assertEquals(req.getStock(), pes.get(0).getStock());
        assertNotNull(pes.get(0).getImageUrls());
        assertTrue(pes.get(0).getImageUrls().containsAll(req.getImageUrls()));
    }

    @Test
    void addProduct_savesCorrectStockStatus() throws Exception {
        ProductCreateRequest req = ProductCreateRequest.builder()
                .name("New product")
                .imageUrls(List.of(
                        "http://images.com/product_new1.jpg",
                        "http://images.com/product_new2.jpg"
                ))
                .price(BigDecimal.valueOf(123.11))
                .category(c1.getName())
                .stock(20)
                .description("New product description")
                .build();
        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stockStatus").value("LOW_STOCK"))
                .andExpect(jsonPath("$.stock").value(20))
        ;
    }

    @Test
    void addProduct_returns403_whenNotAdmin() throws Exception {
        ProductCreateRequest req = ProductCreateRequest.builder()
                .name("New product")
                .imageUrls(List.of(
                        "http://images.com/product_new1.jpg",
                        "http://images.com/product_new2.jpg"
                ))
                .price(BigDecimal.valueOf(123.11))
                .category(c1.getName())
                .description("New product description")
                .build();
        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void updateProductById_returns200_whenValidRequest() throws Exception {
        ProductUpdateRequest req = ProductUpdateRequest.builder()
                .name("New product name")
                .description("New product description")
                .activeStatus(ActiveStatus.INACTIVE)
                .stock(10)
                .category(c3.getName())
                .price(BigDecimal.valueOf(999.99))
                .build();
        mvc.perform(patch("/api/v1/products/{id}", p1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(p1.getId().toString()))
                .andExpect(jsonPath("$.name").value(req.getName()))
                .andExpect(jsonPath("$.stock").value(req.getStock()))
                .andExpect(jsonPath("$.activeStatus").value(req.getActiveStatus().name()))
                .andExpect(jsonPath("$.category.id").value(c3.getId().toString()))
                .andExpect(jsonPath("$.price").value(req.getPrice()))
        ;
        ProductEntity pe = prodRepo.findById(p1.getId())
                .orElseThrow();
        assertEquals(req.getName(), pe.getName());
        assertEquals(ProductEntity.ActiveStatus.valueOf(req.getActiveStatus().name()), pe.getActiveStatus());
        assertEquals(req.getStock(), pe.getStock());
        assertEquals(c3.getId(), pe.getCategory().getId());
        assertEquals(req.getPrice(), pe.getPrice());
        assertEquals(req.getDescription(), pe.getDescription());
        assertEquals(ProductEntity.StockStatus.LOW_STOCK, pe.calculateStockStatus());
        assertTrue(pe.getUpdatedAt().isAfter(pe.getCreatedAt()));
    }

    @Test
    void updateProductById_returns403_whenNotAdmin() throws Exception {
        ProductUpdateRequest req = ProductUpdateRequest.builder()
                .name("New product name")
                .build();
        mvc.perform(patch("/api/v1/products/{id}", p1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden())
                ;
    }

    @Test
    void deleteProductById_returns204_whenValidRequest() throws Exception {
        mvc.perform(delete("/api/v1/products/{id}", p1.getId().toString())
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());

        Optional<ProductEntity> peo = prodRepo.findById(p1.getId());
        assertThat(peo).isEmpty();
    }

    @Test
    void getAllCategories_returns200() throws Exception {
        mvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void createCategory_returns201_whenValidRequest() throws Exception {
        CategoryCreateRequest req = CategoryCreateRequest.builder()
                .name("New category")
                .description("New category description")
        .build();

        mvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(oMapper.writeValueAsString(req))
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(req.getName()))
                .andExpect(jsonPath("$.description").value(req.getDescription()))
                ;

        Optional<CategoryEntity> ce = catRepo.findCategoryByName(req.getName());
        assertThat(ce).isNotEmpty();
        assertEquals(req.getName(), ce.get().getName());
        assertEquals(req.getDescription(), ce.get().getDescription());
    }

    @Test
    void createCategory_returns403_whenNotAdmin() throws Exception {
        CategoryCreateRequest req = CategoryCreateRequest.builder()
                .name("New category")
                .description("New category description")
                .build();

        mvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void updateCategoryById_returns204_whenValidRequest() throws Exception {
        CategoryUpdateRequest req = CategoryUpdateRequest.builder()
                .name("New category name")
                .description("New category description")
        .build();

        mvc.perform(patch("/api/v1/categories/{id}", c1.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(oMapper.writeValueAsString(req))
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());

        Optional<CategoryEntity> ce = catRepo.findCategoryByName(req.getName());
        assertThat(ce).isNotEmpty();
        assertEquals(req.getName(), ce.get().getName());
        assertEquals(req.getDescription(), ce.get().getDescription());
    }

    @Test
    void updateCategoryById_returns403_whenNotAdmin() throws Exception {
        CategoryUpdateRequest req = CategoryUpdateRequest.builder()
                .name("New category name")
                .description("New category description")
                .build();

        mvc.perform(patch("/api/v1/categories/{id}", c1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req))
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCategoryById_returns204_whenValidRequest() throws Exception {
        mvc.perform(delete("/api/v1/categories/{id}", c1.getId().toString())
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());
        Optional<CategoryEntity> ce = catRepo.findById(c1.getId());
        assertThat(ce).isEmpty();

        List<UUID> ids = List.of(p1.getId(), p2.getId());
        List<ProductEntity> pes = prodRepo.findAllById(ids);
        pes.forEach(p -> {
            assertEquals("Uncategorized", p.getCategory().getName());
            assertTrue(p.getUpdatedAt().isAfter(p.getCreatedAt()));
        });
    }

    @Test
    void deleteCategoryById_returns403_whenNotAdmin() throws Exception {
        mvc.perform(delete("/api/v1/categories/{id}", c1.getId().toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());

        Optional<CategoryEntity> ce = catRepo.findById(c1.getId());
        assertThat(ce).isNotEmpty();
    }

    @Test
    void getProductsByCategoryId_returns200_whenValidRequest() throws Exception {
        mvc.perform(get("/api/v1/categories/{id}/products", c1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                ;
    }

    @Test
    void getProductsByCategoryId_returns404_whenCategoryNotExist() throws Exception {
        mvc.perform(get("/api/v1/categories/{id}/products", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound())
                ;

    }
}
