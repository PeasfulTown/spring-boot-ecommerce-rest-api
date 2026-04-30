package xyz.peasfultown.ecommerce.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ProductStockUpdateMessage {
    Map<String, Integer> productIdStockMap;
}
