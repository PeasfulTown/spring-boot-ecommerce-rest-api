package xyz.peasfultown.ecommerce.payment_service.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardToken {
    private String cardId;
    private String token;
    private String lastFourDigits;
    private Integer expiryMonth;
    private Integer expiryYear;
}
