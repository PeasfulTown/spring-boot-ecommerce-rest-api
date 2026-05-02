package xyz.peasfultown.ecommerce.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.peasfultown.ecommerce.payment_service.dto.CardToken;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url}",
        path = "/api/v1/users",
        configuration = { UserServiceErrorDecoder.class }
)
public interface UserServiceClient {
    @GetMapping("/cards/{cardId}/token")
    CardToken getCardToken(@PathVariable("cardId") String cardId);
}
