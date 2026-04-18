package xyz.peasfultown.ecommerce.order_service.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.ecommerce.order_api.model.Order;
import xyz.peasfultown.ecommerce.order_service.entity.OrderEntity;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toModel(OrderEntity entity);
}
