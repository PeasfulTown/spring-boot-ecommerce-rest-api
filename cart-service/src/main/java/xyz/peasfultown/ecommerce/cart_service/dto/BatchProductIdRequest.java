package xyz.peasfultown.ecommerce.cart_service.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchProductIdRequest {
    private List<String> ids;
}
