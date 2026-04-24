package xyz.peasfultown.ecommerce.cart_service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;
import xyz.peasfultown.ecommerce.cart_service.repository.CartItemRepository;
import xyz.peasfultown.ecommerce.cart_service.repository.CartRepository;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles({"test", "rabbitmq"})
@AutoConfigureMockMvc
@Slf4j
public class AsyncTests {
    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private CartItemRepository ciRepo;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void cartCheckout_sendsOrderCreateMessage() throws Exception {
        CartEntity ce = CartEntity.builder()

        .build();
    }
}
