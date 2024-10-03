package comatchingfc.comatchingfc.auth.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenRes {
    private String accessToken;
    private String refreshToken;
}
