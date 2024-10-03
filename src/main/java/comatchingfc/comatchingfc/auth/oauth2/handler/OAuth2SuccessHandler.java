package comatchingfc.comatchingfc.auth.oauth2.handler;

import comatchingfc.comatchingfc.auth.jwt.JwtUtil;
import comatchingfc.comatchingfc.auth.jwt.refresh.service.RefreshTokenService;
import comatchingfc.comatchingfc.auth.oauth2.dto.CustomOAuth2User;
import comatchingfc.comatchingfc.utils.security.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final SecurityUtil securityUtil;

//    @Value("${redirect-url.frontend}")
    private String REDIRECT_URL = "http://localhost:5173/home";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();
        String uuid = user.getName();
        String role = user.getRole();

        String accessToken = jwtUtil.generateAccessToken(uuid, role);
        String refreshToken = refreshTokenService.getRefreshToken(uuid);

        if (refreshToken == null) {
            refreshToken = jwtUtil.generateRefreshToken(uuid, role);
            refreshTokenService.saveRefreshToken(uuid, refreshToken);
        }

        // Access Token을 HttpOnly 쿠키에 설정
        ResponseCookie accessCookie = securityUtil.setAccessResponseCookie(accessToken);

        // Refresh Token을 HttpOnly 쿠키에 설정
        ResponseCookie refreshCookie = securityUtil.setRefreshResponseCookie(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 프론트엔드로 리디렉션
        getRedirectStrategy().sendRedirect(request, response, REDIRECT_URL);

    }
}
