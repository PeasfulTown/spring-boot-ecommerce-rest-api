package xyz.peasfultown.ecommerce.cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xyz.peasfultown.ecommerce.cart_service.config.OpenFeignConfig;
import xyz.peasfultown.ecommerce.cart_service.dto.Address;
import xyz.peasfultown.ecommerce.cart_service.dto.User;
import xyz.peasfultown.ecommerce.cart_service.exception.CustomServiceErrorDecoder;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url}",
        path = "/api/v1",
        configuration = { CustomServiceErrorDecoder.class }
)
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    ResponseEntity<User> getUserById(@PathVariable("id") String userId);

    @GetMapping("/addresses/{id}")
    ResponseEntity<Address> getAddressById(@PathVariable("id") String addressId);
}
