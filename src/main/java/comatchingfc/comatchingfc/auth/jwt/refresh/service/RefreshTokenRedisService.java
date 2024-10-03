package comatchingfc.comatchingfc.auth.jwt.refresh.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static comatchingfc.comatchingfc.auth.jwt.JwtExpirationConst.REFRESH_TOKEN_EXPIRATION;


@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveRefreshToken(String uuid, String refreshToken) {
        redisTemplate.opsForValue().set(uuid, refreshToken, REFRESH_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String uuid) {
        return (String) redisTemplate.opsForValue().get(uuid);
    }
}
