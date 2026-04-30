package xyz.peasfultown.ecommerce.order_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderInformation {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private Address address;
}
