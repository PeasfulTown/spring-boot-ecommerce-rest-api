package xyz.peasfultown.ecommerce.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import xyz.peasfultown.ecommerce.product_api.model.NewCategoryReq;
import xyz.peasfultown.ecommerce.product_api.model.NewProductReq;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_service.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getProductById_shouldReturn200_whenProductExists() throws Exception {
        String productId = "f6a7b8c9-d0e1-2345-fabc-456789012345";
        Product product = new Product();
        product.id(productId)
                .name("iPhone 15")
                .price(BigDecimal.valueOf(999.99));

        when(productService.getProductById(productId)).thenReturn(product);
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.price").value(BigDecimal.valueOf(999.99)));
    }

    @Test
    void addProduct_shouldReturn400_whenNameIsBlank() throws Exception {
        NewProductReq req = new NewProductReq()
                .name("")
                .description("Latest Apple product")
                .imageUrls(List.of(
                        "http://images.com/iphone15_1.png",
                        "http://images.com/iphone15_2.png"
                ))
                .price(BigDecimal.valueOf(999.99))
                .category("Electronics");
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        NewCategoryReq req = new NewCategoryReq()
                .name("");

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}