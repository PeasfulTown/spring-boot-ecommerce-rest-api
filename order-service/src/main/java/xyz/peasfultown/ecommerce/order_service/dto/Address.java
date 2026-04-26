package xyz.peasfultown.ecommerce.order_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Address {
    private String streetNumber;
    private String streetName;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
