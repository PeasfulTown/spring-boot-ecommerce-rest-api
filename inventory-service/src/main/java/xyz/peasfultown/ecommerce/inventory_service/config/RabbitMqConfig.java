package xyz.peasfultown.ecommerce.inventory_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.peasfultown.ecommerce.inventory_service.dto.UpdateInventoryStockMessage;
import xyz.peasfultown.ecommerce.inventory_service.dto.UpdateProductStockStatusMessage;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {
    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.virtual-host}")
    private String vhost;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(RabbitMqConstants.exchange);
    }

    @Bean
    public Queue inventory_stockUpdate_queue() {
        return new Queue(RabbitMqConstants.inventory_stockUpdate_queue);
    }

    @Bean
    public Queue product_stockStatusUpdate_queue() {
        return new Queue(RabbitMqConstants.product_stockStatusUpdate_queue);
    }

    @Bean
    public Binding inventory_stockUpdate_binding(TopicExchange exchange, Queue inventory_stockUpdate_queue) {
        return BindingBuilder.bind(inventory_stockUpdate_queue)
                .to(exchange)
                .with(RabbitMqConstants.cart_checkout_inventory_updateStock_routingKey);
    }

    @Bean
    public Binding product_stockStatusUpdate_binding(TopicExchange exchange, Queue product_stockStatusUpdate_queue) {
        return BindingBuilder.bind(product_stockStatusUpdate_queue)
                .to(exchange)
                .with(RabbitMqConstants.inventory_updateStock_product_updateStockStatus_routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter(DefaultClassMapper classMapper) {
        Jackson2JsonMessageConverter jsonMessageConverter = new Jackson2JsonMessageConverter();
        jsonMessageConverter.setClassMapper(classMapper);
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper defaultClassMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("UpdateInventoryStockMessage", UpdateInventoryStockMessage.class);
        idClassMapping.put("UpdateProductStockStatusMessage", UpdateProductStockStatusMessage.class);
        defaultClassMapper.setIdClassMapping(idClassMapping);
        return defaultClassMapper;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(this.host);
        connectionFactory.setPort(this.port);
        connectionFactory.setVirtualHost(this.vhost);
        connectionFactory.setUsername(this.username);
        connectionFactory.setPassword(this.password);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, TopicExchange exchange, Jackson2JsonMessageConverter jsonConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(exchange.getName());
        rabbitTemplate.setMessageConverter(jsonConverter);
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

}
