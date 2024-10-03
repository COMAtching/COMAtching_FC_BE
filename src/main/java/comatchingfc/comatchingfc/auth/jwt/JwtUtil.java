package comatchingfc.comatchingfc.auth.jwt;

import comatchingfc.comatchingfc.user.enums.UserRole;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static comatchingfc.comatchingfc.auth.jwt.JwtExpirationConst.*;

@Component
public class JwtUtil {

    private SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));

            secretKey = new SecretKeySpec(hash, "HmacSHA256");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found.", e);
        }
    }

    public String getUUID(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("uuid", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    public String generateAccessToken(String uuid, String role) {
        if (role.equals(UserRole.ROLE_USER.toString()) || role.equals(UserRole.ROLE_PENDING.toString())) {
            return generateToken(uuid, role, USER_ACCESS_TOKEN_EXPIRATION);
        }
        return generateToken(uuid, role, ADMIN_ACCESS_TOKEN_EXPIRATION);
    }
    public String generateRefreshToken(String uuid, String role) {
        return generateToken(uuid, role, REFRESH_TOKEN_EXPIRATION);
    }

    public String generateToken(String uuid, String role, long expiredMs) {
        return Jwts.builder()
                .claim("uuid", uuid)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}
