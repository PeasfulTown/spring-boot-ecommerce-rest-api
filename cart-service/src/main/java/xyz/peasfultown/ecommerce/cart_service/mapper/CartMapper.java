package xyz.peasfultown.ecommerce.cart_service.mapper;

import org.mapstruct.*;
import xyz.peasfultown.ecommerce.cart_api.model.Cart;
import xyz.peasfultown.ecommerce.cart_api.model.CartItem;
import xyz.peasfultown.ecommerce.cart_api.model.Product;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mapper(
    componentModel = "spring",
    uses = { CartItemMapper.class }
)
public interface CartMapper {
    Cart toModel(CartEntity entity);
    Cart toModel(CartEntity entity, @Context Map<String, Product> productMap);

    @AfterMapping
    default void setNumbers(@MappingTarget Cart cart) {
        cart.setTotalItems(cart.getItems().stream()
                .mapToInt(CartItem::getQuantity).sum());

        Function<CartItem, BigDecimal> subtotal = ci ->
                ci.getProductPrice()
                                .multiply(BigDecimal.valueOf(ci.getQuantity()));
        cart.setTotalPrice(cart.getItems().stream()
            .map(subtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}

