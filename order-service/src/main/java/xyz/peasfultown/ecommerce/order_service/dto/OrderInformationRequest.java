package xyz.peasfultown.ecommerce.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class OrderInformationRequest {
    private String userId;
    private String addressId;
}
