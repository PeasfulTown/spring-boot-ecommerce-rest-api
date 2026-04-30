package xyz.peasfultown.ecommerce.auth_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
}
