package xyz.peasfultown.ecommerce.cart_service.mapper;

import org.mapstruct.*;
import xyz.peasfultown.ecommerce.cart_api.model.CartItem;
import xyz.peasfultown.ecommerce.cart_api.model.Product;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItem toModel(CartItemEntity entity, @Context Product product);

    default List<CartItem> toModel(List<CartItemEntity> entities, @Context Map<String, Product> productMap) {
        return entities.stream().map(e ->
            this.toModel(e, productMap.get(e.getProductId().toString()))).toList();
    }

    @AfterMapping
    default void setRemaining(@MappingTarget CartItem cartItem, @Context Product product) {
        cartItem.setProductName(product.getName());
        cartItem.setProductPrice(product.getPrice());
        cartItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
    }

}
