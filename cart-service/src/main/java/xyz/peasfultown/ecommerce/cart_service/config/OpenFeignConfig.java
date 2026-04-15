package xyz.peasfultown.ecommerce.cart_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.peasfultown.ecommerce.cart_service.exception.CustomProductServiceErrorDecoder;

@Configuration
public class OpenFeignConfig {
    @Bean
    public CustomProductServiceErrorDecoder customProductServiceErrorDecoder() {
        return new CustomProductServiceErrorDecoder();
    }
}
