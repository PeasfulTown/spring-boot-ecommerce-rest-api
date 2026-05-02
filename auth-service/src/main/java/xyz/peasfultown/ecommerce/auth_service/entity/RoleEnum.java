package xyz.peasfultown.ecommerce.auth_service.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.security.core.GrantedAuthority;

public enum RoleEnum implements GrantedAuthority {
    ADMIN,
    CUSTOMER;

    @JsonCreator
    public static RoleEnum fromValue(String value) {
        for (RoleEnum e: RoleEnum.values()) {
            if (e.name().equalsIgnoreCase(value))
                return e;
        }
        throw new IllegalArgumentException(String.format(
                "Unexpected role value: %s", value
        ));
    }

    @Override
    public String toString() {
        return this.name();
    }

    @Override
    @JsonValue
    public String getAuthority() {
        return this.name();
    }

}
