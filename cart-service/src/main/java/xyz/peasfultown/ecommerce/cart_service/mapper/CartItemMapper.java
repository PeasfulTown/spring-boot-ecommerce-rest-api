package xyz.peasfultown.ecommerce.cart_service.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.ecommerce.cart_api.model.CartItem;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItem toModel(CartItemEntity entity);

    List<CartItem> toModel(List<CartItemEntity> entities);
}
