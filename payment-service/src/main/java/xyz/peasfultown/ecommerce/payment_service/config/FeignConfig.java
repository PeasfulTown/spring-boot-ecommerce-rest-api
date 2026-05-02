package xyz.peasfultown.ecommerce.payment_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class FeignConfig {
    @Value("${services.internal-secret}")
    private String internalSecret;

    @Bean
    public RequestInterceptor internalServiceInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Internal-Service-Secret", internalSecret);
            requestTemplate.header("X-User-Role", "ADMIN");
        };
    }
}
