package xyz.peasfultown.ecommerce.product_service.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStockUpdateMessageDto {
    Map<String, Integer> content;
}
