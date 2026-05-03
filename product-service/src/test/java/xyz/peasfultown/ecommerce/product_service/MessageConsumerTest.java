package xyz.peasfultown.ecommerce.product_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import xyz.peasfultown.ecommerce.product_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.product_service.dto.ProductStockUpdateMessage;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;
import xyz.peasfultown.ecommerce.product_service.repository.CategoryRepository;
import xyz.peasfultown.ecommerce.product_service.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles({ "test", "rabbitmq" })
@Slf4j
public class MessageConsumerTest {
    @Autowired
    private ProductRepository prodRepo;

    @Autowired
    private CategoryRepository catRepo;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper oMapper;

    private CategoryEntity ce;

    private ProductEntity p1;
    private ProductEntity p2;

    @BeforeEach
    void setup() {
        ce = CategoryEntity.builder()
                .id(UUID.randomUUID())
                .name("Category 1")
                .description("Description of category 1")
                .build();
        catRepo.save(ce);

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
                .category(ce)
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
                .stock(10)
                .category(ce)
                .build();
        prodRepo.saveAll(List.of(p1, p2));
    }

    @Test
    void handleStockUpdateMessages_updatesDb() throws Exception {
        Map<String, Integer> map = new HashMap<>();
        map.put(p1.getId().toString(), 10);
        map.put(p2.getId().toString(), 5);
        ProductStockUpdateMessage dto = ProductStockUpdateMessage.builder()
                .productIdStockMap(map).build();

        Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(dto))
                        .setHeader(RabbitMqConstants.TYPEID_HEADER, "ProductStockUpdateMessageDto")
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                                .build();

        rabbitTemplate.send(RabbitMqConstants.cart_checkout_product_updateStock_routingKey, message);

        await().atMost(10, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    ProductEntity pe = prodRepo.findById(p1.getId()).orElseThrow();
                    return pe.getStock() == 40;
                });

        Map<UUID, ProductEntity> orig = List.of(p1, p2).stream().collect(Collectors.toMap(
                ProductEntity::getId, Function.identity()
        ));
        Map<UUID, ProductEntity> actual = prodRepo.findAllById(List.of(p1.getId(), p2.getId()))
                .stream().collect(Collectors.toMap(ProductEntity::getId, Function.identity()));
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            ProductEntity pe = actual.get(UUID.fromString(e.getKey()));
            assertEquals(e.getValue(),
            orig.get(UUID.fromString(e.getKey())).getStock() - pe.getStock());
        }
    }
}
