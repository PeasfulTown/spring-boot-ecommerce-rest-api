package xyz.peasfultown.ecommerce.auth_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import xyz.peasfultown.ecommerce.auth_api.model.Account;
import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;
import xyz.peasfultown.ecommerce.auth_service.entity.RoleEnum;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account entityToModel(AccountEntity entity);

    @ValueMapping(target = "ADMIN", source = "ADMIN")
    @ValueMapping(target = "CUSTOMER", source = "CUSTOMER")
    Account.RoleEnum toRoleEnum(RoleEnum roleEnum);
}
