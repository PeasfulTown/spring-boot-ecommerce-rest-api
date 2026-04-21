package xyz.peasfultown.ecommerce.inventory_service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.Update;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import xyz.peasfultown.ecommerce.inventory_api.model.Inventory;
import xyz.peasfultown.ecommerce.inventory_api.model.PagedInventoryResponse;
import xyz.peasfultown.ecommerce.inventory_api.model.UpdateInventoryReq;
import xyz.peasfultown.ecommerce.inventory_service.entity.InventoryEntity;
import xyz.peasfultown.ecommerce.inventory_service.repository.InventoryRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Slf4j
@Transactional
public class IntegrationTest {
    @Autowired
    private InventoryRepository invRepo;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper oMapper;

    @Test
    void getAllProductInventory_returns200() throws Exception {
        mvc.perform(get("/api/v1/inventory"))
                .andExpect(status().isOk());
    }

    @Test
    void getProductInventoryByProductId_returns200() throws Exception {
        mvc.perform(get("/api/v1/inventory/{id}", "22222222-2222-2222-2222-222222222220"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(50)));
    }

    @Test
    void getProductInventoryByProductId_returns404_whenProductNotExist() throws Exception {
        String json = mvc.perform(get("/api/v1/inventory/{id}", "33333333-3333-3333-3333-333333333333"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString()
                ;

        // TODO: remove
        ProblemDetail details = oMapper.readValue(json, ProblemDetail.class);
        log.info("Problem Detail: {}", details);
    }

    @Test
    void updateProductInventoryById_returns204_whenValidInput() throws Exception {
        UpdateInventoryReq req = UpdateInventoryReq.builder()
                .stock(99)
                .build();
        mvc.perform(patch("/api/v1/inventory/{id}", "22222222-2222-2222-2222-222222222221")
                .header("X-User-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(oMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent())
                ;

        InventoryEntity ie = invRepo.findInventoryByProductId(UUID.fromString("22222222-2222-2222-2222-222222222221"))
                .get();
        assertEquals(99, ie.getStock());
    }

    @Test
    void updateProductInventoryById_returns403_whenNotAdmin() throws Exception {
        UpdateInventoryReq req = UpdateInventoryReq.builder()
                .stock(99)
                .build();
        mvc.perform(patch("/api/v1/inventory/{id}", "22222222-2222-2222-2222-222222222221")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                ;
    }

    @Test
    void getLowStockProducts_returns200() throws Exception {
        String json = mvc.perform(get("/api/v1/inventory/low-stock")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PagedInventoryResponse pagedResponse = oMapper.readValue(json, new TypeReference<PagedInventoryResponse>(){});
        assertThat(pagedResponse.getContent()).isNotEmpty();
        List<Inventory> invList = pagedResponse.getContent();
        assertThat(invList).isNotEmpty();
        assertEquals(5, invList.size());
        invList.stream()
                .forEach(i -> assertThat(i.getStock()).isLessThanOrEqualTo(10));
    }

    @Test
    void getLowStockProducts_returns403_whenNotAdmin() throws Exception {
        mvc.perform(get("/api/v1/inventory/low-stock")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOutOfStockProducts_returns200() throws Exception {
        String json = mvc.perform(get("/api/v1/inventory/out-of-stock")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PagedInventoryResponse pagedRes = oMapper.readValue(json, new TypeReference<PagedInventoryResponse>() {});
        assertThat(pagedRes.getContent()).isNotEmpty();
        List<Inventory> invList = pagedRes.getContent();
        invList.stream()
                .forEach(i -> assertThat(i.getStock()).isEqualTo(0));
    }

    @Test
    void getOutOfStockProducts_returns403_whenNotAdmin() throws Exception {
        mvc.perform(get("/api/v1/inventory/out-of-stock")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }
}
