package xyz.peasfultown.ecommerce.cart_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private String id;
    private String firstName;
    private String lastName;
    private String number;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
