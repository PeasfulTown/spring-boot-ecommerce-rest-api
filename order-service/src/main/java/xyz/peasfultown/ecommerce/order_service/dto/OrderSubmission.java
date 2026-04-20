package xyz.peasfultown.ecommerce.order_service.dto;

import lombok.*;
import xyz.peasfultown.ecommerce.order_api.model.CartItem;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSubmission {
    private String userId;
    private String contactEmail;
    private String contactPhone;
    private String addressNumber;
    private String addressStreet;
    private String addressCity;
    private String addressState;
    private String addressCountry;
    private String addressPostalCode;
    private BigDecimal orderTotal;
    private int orderItemCount;
    private List<CartItem> items;

    @Override
    public String toString() {
        return "OrderSubmission{" +
                "userId='" + userId + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", addressNumber='" + addressNumber + '\'' +
                ", addressStreet='" + addressStreet + '\'' +
                ", addressCity='" + addressCity + '\'' +
                ", addressState='" + addressState + '\'' +
                ", addressCountry='" + addressCountry + '\'' +
                ", addressPostalCode='" + addressPostalCode + '\'' +
                ", orderTotal=" + orderTotal +
                ", orderItemCount=" + orderItemCount +
                ", items=" + items +
                '}';
    }
}
