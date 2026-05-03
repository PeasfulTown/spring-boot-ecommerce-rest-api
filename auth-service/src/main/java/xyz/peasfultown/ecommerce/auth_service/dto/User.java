package xyz.peasfultown.ecommerce.auth_service.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
}
