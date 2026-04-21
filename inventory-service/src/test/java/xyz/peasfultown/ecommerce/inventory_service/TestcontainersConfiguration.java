package xyz.peasfultown.ecommerce.inventory_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
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
	DynamicPropertyRegistrar propertyRegistrar(MySQLContainer<?> mysqlContainer) {
		return registry -> {
			registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
			registry.add("spring.datasource.username", mysqlContainer::getUsername);
			registry.add("spring.datasource.password", mysqlContainer::getPassword);
		};
	}

//	@Bean
//	@ServiceConnection
//	RabbitMQContainer rabbitContainer() {
//		return new RabbitMQContainer(DockerImageName.parse("rabbitmq:latest"));
//	}

}
