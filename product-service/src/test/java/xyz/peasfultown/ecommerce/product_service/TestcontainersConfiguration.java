package xyz.peasfultown.ecommerce.product_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");
        mysql.withDatabaseName("ecommerce_inventory_testdb");
        mysql.withUsername("testuser");
        mysql.withPassword("testpassword");
        return mysql;
    }

    @Bean
    DynamicPropertyRegistrar propertyRegistrarMysql(MySQLContainer<?> mysqlContainer) {
        return registry -> {
            registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
            registry.add("spring.datasource.username", mysqlContainer::getUsername);
            registry.add("spring.datasource.password", mysqlContainer::getPassword);
        };
    }


    @Profile( { "test", "rabbitmq"} )
    @Bean
    @ServiceConnection
    RabbitMQContainer rabbitContainer() {
        RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:4-management");
        rabbitMQContainer.withAdminUser("testuser");
        rabbitMQContainer.withAdminPassword("testpassword");
        return rabbitMQContainer;
    }

    @Profile( { "test", "rabbitmq"} )
    @Bean
    DynamicPropertyRegistrar propertyRegistrarRabbitMq(RabbitMQContainer rabbitMQContainer) {
        return registry -> {
            registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
            registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
            registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
            registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
        };
    }
}
