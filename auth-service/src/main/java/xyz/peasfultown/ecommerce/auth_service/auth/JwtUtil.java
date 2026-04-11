package xyz.peasfultown.ecommerce.auth_service.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiry.accessToken}")
    private long accessTokenExpiry;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateAccessToken(AccountEntity acc) {
        return Jwts.builder()
                .subject(acc.getId().toString())
                .claim("email", acc.getEmail())
                .claim("role", acc.getRole().getAuthority())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }
}
