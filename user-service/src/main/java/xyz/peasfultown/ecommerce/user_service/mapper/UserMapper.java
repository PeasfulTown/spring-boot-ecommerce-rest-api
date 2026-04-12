package xyz.peasfultown.ecommerce.user_service.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.ecommerce.user_api.model.User;
import xyz.peasfultown.ecommerce.user_service.entity.UserEntity;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {
    abstract User entityToModel(UserEntity ue);

    default OffsetDateTime map(Instant instant) {
        if (instant == null) return null;
        return instant.atOffset(ZoneOffset.UTC);
    }

    default Instant map(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) return null;
        return offsetDateTime.toInstant();
    }

    default String map(UUID uuid) {
        if (uuid == null) return null;
        return uuid.toString();
    }

    default UUID map (String uuid) {
        if (uuid == null) return null;
        return UUID.fromString(uuid);
    }
}
