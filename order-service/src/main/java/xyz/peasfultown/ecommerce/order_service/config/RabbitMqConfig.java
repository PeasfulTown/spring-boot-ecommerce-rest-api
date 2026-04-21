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
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public Queue orderSubmitted_queue() {
        return new Queue(RabbitMqConstants.orderSubmitted_queue);
    }

    @Bean
    public Binding orderSubmitted_queue_binding(Queue orderSubmitted_queue,
                                                TopicExchange exchange) {
        return BindingBuilder.bind(orderSubmitted_queue).to(exchange).with(RabbitMqConstants.orderSubmitted_routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
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
