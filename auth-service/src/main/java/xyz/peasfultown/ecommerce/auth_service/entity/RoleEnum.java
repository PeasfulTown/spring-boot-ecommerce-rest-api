package xyz.peasfultown.ecommerce.auth_service.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.security.core.GrantedAuthority;

public enum RoleEnum implements GrantedAuthority {
    ADMIN(Const.ADMIN),
    USER(Const.USER);

    private final String authority;

    RoleEnum(String authority) {
        this.authority = authority;
    }

    @JsonCreator
    public static RoleEnum fromAuthority(String authority) {
        for (RoleEnum e: RoleEnum.values()) {
            if (e.toString().equalsIgnoreCase(authority))
                return e;
        }
        throw new IllegalArgumentException(String.format(
                "Unexpected authority value: %s", authority
        ));
    }

    @Override
    public String toString() {
        return String.valueOf(authority);
    }

    @Override
    @JsonValue
    public String getAuthority() {
        return authority;
    }

    public class Const {
        private static final String ADMIN = "ROLE_ADMIN";
        private static final String USER = "ROLE_USER";
    }
}
