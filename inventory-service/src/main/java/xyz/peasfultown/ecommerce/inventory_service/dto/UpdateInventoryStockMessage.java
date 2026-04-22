package xyz.peasfultown.ecommerce.inventory_service.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInventoryStockMessage {
    private Map<String, Integer> items;
}
