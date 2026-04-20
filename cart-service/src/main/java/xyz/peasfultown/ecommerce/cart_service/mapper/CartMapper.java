package xyz.peasfultown.ecommerce.cart_service.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.ecommerce.cart_api.model.Cart;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;

@Mapper(componentModel = "spring")
public interface CartMapper {
    Cart toModel(CartEntity entity);
}
