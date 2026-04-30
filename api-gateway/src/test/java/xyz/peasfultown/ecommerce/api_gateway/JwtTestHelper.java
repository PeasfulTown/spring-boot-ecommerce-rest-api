package xyz.peasfultown.ecommerce.api_gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtTestHelper {
    private static String TEST_SECRET = "YWdlc3VwcGVydmVzc2Vsc25lY2tsZXRjb21wYXNzc3RheWFuY2llbnRzbGVlcGV4Y2U=";

    @Value("${jwt.secret}")
    public void setTestSecret(String secret) {
        TEST_SECRET = secret;
    }

    public static String generateToken(String userId, String role) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900_000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public static String generateExpiredToken(String userId, String role) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis() - 1_000_000))
                .expiration(new Date(System.currentTimeMillis() - 900_000))
                .signWith(key)
                .compact();
    }
}
