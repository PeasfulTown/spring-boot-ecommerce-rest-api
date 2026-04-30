package xyz.peasfultown.ecommerce.user_service.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.ecommerce.user_api.model.Card;
import xyz.peasfultown.ecommerce.user_service.entity.CardEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {
    Card toModel(CardEntity cardEntity);

    List<Card> toModel(List<CardEntity> cardEntities);
}
