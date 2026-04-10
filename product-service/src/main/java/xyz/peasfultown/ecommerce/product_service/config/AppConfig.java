package xyz.peasfultown.ecommerce.product_service.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AppConfig {

    @Bean
    @Profile("local")
    public FlywayMigrationStrategy cleanMigrationStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }

}
