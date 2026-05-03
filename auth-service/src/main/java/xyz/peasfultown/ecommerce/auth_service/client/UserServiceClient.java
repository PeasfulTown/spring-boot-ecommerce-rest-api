package xyz.peasfultown.ecommerce.auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.peasfultown.ecommerce.auth_service.config.FeignConfig;
import xyz.peasfultown.ecommerce.auth_service.dto.User;
import xyz.peasfultown.ecommerce.auth_service.dto.UserCreateRequest;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url}",
        path = "/api/v1/users",
        configuration = { UserServiceErrorDecoder.class }
)
public interface UserServiceClient {
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest req);
}
