package xyz.peasfultown.ecommerce.product_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;
import xyz.peasfultown.ecommerce.product_service.mapper.ProductMapper;
import xyz.peasfultown.ecommerce.product_service.repository.CategoryRepository;
import xyz.peasfultown.ecommerce.product_service.repository.ProductRepository;
import xyz.peasfultown.ecommerce.product_service.service.ProductServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository repo;

    @Mock
    private CategoryRepository caRepo;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    void getProductById_shouldReturnProduct_whenProductExists() {
        // arrange
        String productId = "f6a7b8c9-d0e1-2345-fabc-456789012345";
        ProductEntity pe = ProductEntity.builder()
                .id(UUID.fromString(productId))
                .name("iPhone 15")
                .build();
        Product expected = new Product()
                .id(productId)
                .name("iPhone 15");

        when(repo.findById(UUID.fromString(productId))).thenReturn(Optional.of(pe));
        when(mapper.entityToModel(pe)).thenReturn(expected);

        // act
        Product result = service.getProductById(productId);

        // assert
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo(expected.getName());
        verify(repo).findById(UUID.fromString(productId));
    }
}
