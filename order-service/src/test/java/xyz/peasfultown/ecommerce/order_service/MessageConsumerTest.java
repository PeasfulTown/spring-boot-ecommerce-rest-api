package xyz.peasfultown.ecommerce.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import xyz.peasfultown.ecommerce.order_api.model.OrderItem;
import xyz.peasfultown.ecommerce.order_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.order_service.dto.OrderCreateMessage;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;
import xyz.peasfultown.ecommerce.order_service.entity.OrderItemEntity;
import xyz.peasfultown.ecommerce.order_service.repository.OrderItemRepository;
import xyz.peasfultown.ecommerce.order_service.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles({"test", "rabbitmq"})
@Slf4j
public class MessageConsumerTest {
    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRepository oiRepo;

    @Autowired
    private ObjectMapper oMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void consumeOrderCreateMessage_whenCartCheckout_savesInDb() throws Exception {
        OrderItem ci1 = OrderItem.builder()
                .productId(UUID.randomUUID().toString())
                .productName("Product 1")
                .productPrice(BigDecimal.valueOf(111.11))
                .quantity(2)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        OrderItem ci2 = OrderItem.builder()
                .productId(UUID.randomUUID().toString())
                .productName("Product 2")
                .productPrice(BigDecimal.valueOf(222.22))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(222.22))
                .build();
        OrderCreateMessage messageBody = OrderCreateMessage.builder()
                .userId(UUID.randomUUID().toString())
                .email("email@example.com")
                .phone("1234567899")
                .streetNumber("123")
                .streetName("Street St")
                .city("City")
                .state("State")
                .country("Country")
                .postalCode("111AAA")
                .totalPrice(BigDecimal.valueOf(444.44))
                .itemCount(3)
                .items(List.of(ci1, ci2))
                .build();

        Message message = MessageBuilder.withBody(oMapper.writeValueAsBytes(messageBody))
                        .setHeader("__TypeId__", OrderCreateMessage.class.getSimpleName())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        rabbitTemplate.send(RabbitMqConstants.cart_checkout_order_createOrder_routingKey, message);
        await().atMost(10, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> orderRepo.count() > 0);

        Optional<OrderEntity> oeo = Optional.of(orderRepo.findAll().get(0));
        assertThat(oeo).isPresent();
        OrderEntity oe = oeo.get();

        assertEquals(messageBody.getUserId(), oe.getUserId().toString());
        assertEquals(messageBody.getEmail(), oe.getEmail());
        assertEquals(messageBody.getPhone(), oe.getPhone());
        assertEquals(messageBody.getStreetNumber(), oe.getStreetNumber());
        assertEquals(messageBody.getStreetName(), oe.getStreetName());
        assertEquals(messageBody.getCity(), oe.getCity());
        assertEquals(messageBody.getState(), oe.getState());
        assertEquals(messageBody.getCountry(), oe.getCountry());
        assertEquals(messageBody.getTotalPrice(), oe.getTotalPrice());
        assertEquals(messageBody.getItemCount(), oe.getItemCount());

        List<OrderItemEntity> ois = oiRepo.findOrderItemsByOrderId(oe.getId());
        assertEquals(2, ois.size());

        Map<String, OrderItem> orderItemMap = messageBody.getItems().stream().collect(Collectors.toMap(o ->
               o.getProductId(), Function.identity()
        ));
        ois.forEach(o -> {
            Optional<OrderItem> oio = Optional.ofNullable(orderItemMap.get(o.getProductId().toString()));
            assertThat(oio).isNotEmpty();
            OrderItem oi = oio.get();
            assertEquals(oi.getProductName(), o.getProductName());
            assertEquals(oi.getProductPrice(), o.getProductPrice());
            assertEquals(oi.getQuantity(), o.getQuantity());
            assertEquals(oi.getSubtotal(), o.getSubtotal());
        });
    }

}
