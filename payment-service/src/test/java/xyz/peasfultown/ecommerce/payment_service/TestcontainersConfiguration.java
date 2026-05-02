package xyz.peasfultown.ecommerce.payment_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	MySQLContainer<?> mysqlContainer() {
		MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8")
				.withDatabaseName("ecommerce_auth_testdb")
				.withUsername("testuser")
				.withPassword("testpassword");
		return mysql;
	}

	@Bean
	DynamicPropertyRegistrar configureProperties(MySQLContainer mysqlContainer) {
		return registry -> {
			registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
			registry.add("spring.datasource.username", mysqlContainer::getUsername);
			registry.add("spring.datasource.password", mysqlContainer::getPassword);
			registry.add("spring.datasource.name", mysqlContainer::getDatabaseName);
		};
	}

	@Profile({ "test", "rabbitmq" })
	@Bean
	RabbitMQContainer rabbitContainer() {
		RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:4-management");
		rabbitMQContainer.withAdminUser("testuser");
		rabbitMQContainer.withAdminPassword("testpassword");
		return rabbitMQContainer;
	}

	@Profile({ "test", "rabbitmq" })
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
