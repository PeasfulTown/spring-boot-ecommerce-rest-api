package xyz.peasfultown.ecommerce.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.peasfultown.ecommerce.user_api.model.Address;
import xyz.peasfultown.ecommerce.user_service.entity.AddressEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "isPrimary", source = "primary")
    Address toModel(AddressEntity entity);

    List<Address> toModel(List<AddressEntity> entity);
}
