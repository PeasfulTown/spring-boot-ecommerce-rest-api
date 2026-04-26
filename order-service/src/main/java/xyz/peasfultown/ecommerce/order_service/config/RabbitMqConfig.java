package xyz.peasfultown.ecommerce.order_service.config;

import lombok.extern.slf4j.Slf4j;
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
import xyz.peasfultown.ecommerce.order_service.dto.OrderConfirmationMessage;
import xyz.peasfultown.ecommerce.order_service.dto.PaymentConfirmationMessage;
import xyz.peasfultown.ecommerce.order_service.dto.OrderCreateMessage;

import java.util.HashMap;
import java.util.Map;

@Slf4j
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
    public Queue order_createOrder_queue() {
        return new Queue(RabbitMqConstants.order_createOrder_queue);
    }

    @Bean
    public Queue order_confirmOrder_queue() {
        return new Queue(RabbitMqConstants.order_confirmOrder_queue);
    }

    @Bean
    public Queue payment_confirmPayment_queue() {
        return new Queue(RabbitMqConstants.payment_confirmPayment_queue);
    }

    @Bean
    public Binding cart_checkout_order_createOrder_binding(Queue order_createOrder_queue,
                                                           TopicExchange exchange) {
        return BindingBuilder.bind(order_createOrder_queue).to(exchange).with(RabbitMqConstants.cart_checkout_order_createOrder_routingKey);
    }

    @Bean
    public Binding cart_checkout_order_confirmOrder_binding(Queue order_confirmOrder_queue,
                                                            TopicExchange exchange) {
        return BindingBuilder.bind(order_confirmOrder_queue).to(exchange).with(RabbitMqConstants.cart_checkout_order_confirmOrder_routingKey);
    }

    @Bean
    public Binding cart_checkout_payment_confirmPayment_binding(Queue payment_confirmPayment_queue,
    TopicExchange exchange) {
        return BindingBuilder.bind(payment_confirmPayment_queue).to(exchange).with(RabbitMqConstants.cart_checkout_payment_confirmPayment_routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter(DefaultClassMapper classMapper) {
        Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
        jsonConverter.setClassMapper(classMapper);
        return jsonConverter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMap = new HashMap<>();
        idClassMap.put("OrderCreateMessage", OrderCreateMessage.class);
        idClassMap.put("CardIdMessage", PaymentConfirmationMessage.class);
        idClassMap.put("OrderConfirmationMessage", OrderConfirmationMessage.class);
        classMapper.setIdClassMapping(idClassMap);
        return classMapper;
    }


    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(this.host);
        connectionFactory.setPort(this.port);
        connectionFactory.setUsername(this.username);
        connectionFactory.setPassword(this.password);
        connectionFactory.setVirtualHost(this.vhost);
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
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        return rabbitAdmin;
    }

//    @Bean
//    public SimpleMessageListenerContainer submittedOrdersMessageListenerContainer(ConnectionFactory connectionFactory, Queue ordersSubmitted_queue) {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//        container.addQueueNames(RabbitMqConstants.ordersSubmitted_queue);
//        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter();
//    }


}
