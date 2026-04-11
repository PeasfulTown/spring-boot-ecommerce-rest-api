package xyz.peasfultown.ecommerce.auth_service.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.ecommerce.auth_api.model.Account;
import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account entityToModel(AccountEntity entity);
}
