package xyz.peasfultown.ecommerce.inventory_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import xyz.peasfultown.ecommerce.inventory_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.inventory_service.dto.UpdateInventoryStockMessage;
import xyz.peasfultown.ecommerce.inventory_service.dto.UpdateProductStockStatusMessage;
import xyz.peasfultown.ecommerce.inventory_service.entity.InventoryEntity;
import xyz.peasfultown.ecommerce.inventory_service.repository.InventoryRepository;
import xyz.peasfultown.ecommerce.inventory_service.service.InventoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@Import(TestcontainersConfiguration.class)
@ActiveProfiles({"test", "rabbitmq"})
@SpringBootTest
@Slf4j
public class MessageConsumerTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private InventoryRepository invRepo;

    @Autowired
    private InventoryService invService;

    @Autowired
    private Queue order_stockUpdate_queue;

    @Autowired
    private ObjectMapper oMapper;

    private InventoryEntity ie1;
    private InventoryEntity ie2;
    private InventoryEntity ie3;

    @BeforeEach
    void setup() {
        ie1 = InventoryEntity.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111110"))
                .productId(UUID.fromString("22222222-2222-2222-2222-222222222220"))
                .stock(50)
                .build();
        ie2 = InventoryEntity.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .productId(UUID.fromString("22222222-2222-2222-2222-222222222221"))
                .stock(11)
                .build();
        ie3 = InventoryEntity.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111112"))
                .productId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .stock(4)
                .build();

        invRepo.saveAll(List.of(ie1, ie2, ie3));
        assertThat(invRepo.findAll()).hasSize(3);
    }

    @Test
    void handleOrderSubmissionMessages_updatesInventoryInDb() throws Exception {
        Map<String, Integer> itemQuantityMap = new HashMap<>();
        itemQuantityMap.put("22222222-2222-2222-2222-222222222220", 10);
        itemQuantityMap.put("22222222-2222-2222-2222-222222222221", 2);
        itemQuantityMap.put("22222222-2222-2222-2222-222222222222", 4);

        UpdateInventoryStockMessage messageBody = UpdateInventoryStockMessage.builder()
                .items(itemQuantityMap)
                .build();
        log.info("Sending message body as json: {}", oMapper.writeValueAsString(messageBody));
        Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(messageBody))
                .setHeader("__TypeId__", "UpdateInventoryMessage")
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        rabbitTemplate.send("order.submitted.update-inventory", message);
        await().atMost(10, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    InventoryEntity ie = invRepo.findInventoryByProductId(UUID.fromString("22222222-2222-2222-2222-222222222220")).orElseThrow();
                    return ie.getStock() == 40;
                })
                ;

        Map<UUID, InventoryEntity> invMap = invRepo.findInventoriesByProductIds(itemQuantityMap.keySet().stream().map(UUID::fromString).toList())
                .stream().collect(Collectors.toMap(InventoryEntity::getProductId, Function.identity()));
        assertEquals(40, invMap.get(UUID.fromString("22222222-2222-2222-2222-222222222220")).getStock());
        assertEquals(8, invMap.get(UUID.fromString("22222222-2222-2222-2222-222222222221")).getStock());
        assertEquals(0, invMap.get(UUID.fromString("22222222-2222-2222-2222-222222222222")).getStock());

        UpdateProductStockStatusMessage updateProductStockStatusMessage = rabbitTemplate.receiveAndConvert("product.stock-status-update",
                new ParameterizedTypeReference<UpdateProductStockStatusMessage>() {
                });
        // TODO: wait
        assertNotNull(updateProductStockStatusMessage);
        assertEquals("OUT_OF_STOCK", updateProductStockStatusMessage.getContent().get("22222222-2222-2222-2222-222222222222"));
    }

}
