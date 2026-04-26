package xyz.peasfultown.ecommerce.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import xyz.peasfultown.ecommerce.order_service.dto.OrderInformation;
import xyz.peasfultown.ecommerce.order_service.dto.UserIdAndAddressIdRequest;

@FeignClient(
    name = "user-client",
    url = "${services.user-service.url}",
    path = "/api/v1/users",
    configuration = { UserServiceErrorDecoder.class }
)
public interface UserServiceClient {
    @PostMapping("/order-info")
    OrderInformation getOrderInformation(UserIdAndAddressIdRequest req);
}
