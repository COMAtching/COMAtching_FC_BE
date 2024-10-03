package comatchingfc.comatchingfc.auth.jwt;

import comatchingfc.comatchingfc.auth.oauth2.dto.CustomOAuth2User;
import comatchingfc.comatchingfc.auth.oauth2.dto.UserDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private static final List<String> WHITELIST = List.of(
            "/login",
            "/auth/refresh",
            "/admin/register",
            "/admin/login"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.info("요청 URL = {}", requestURI);
        if (WHITELIST.stream().anyMatch(requestURI::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getTokenFromCookies(request, "accessToken");

        try {
            if (accessToken != null && !jwtUtil.isExpired(accessToken)) {
                log.info("엑세스 토큰 유효");
                setAuthentication(accessToken);
                filterChain.doFilter(request, response);
                return;
            }
        } catch (ExpiredJwtException e) {
            log.info("엑세스 토큰 만료");
            throw new JwtException("TOKEN_EXPIRED");
        } catch (SignatureException e) {
            log.info("엑세스 토큰 무결성 오류");
            throw new JwtException("TOKEN_INVALID");
        } catch (JwtException e) {
            log.info("엑세스 토큰 오류");
            throw new JwtException("TOKEN_INVALID");
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String accessToken) {
        String uuid = jwtUtil.getUUID(accessToken);
        String role = jwtUtil.getRole(accessToken);

        UserDto userDto = new UserDto();
        userDto.setUuid(uuid);
        userDto.setRole(role);

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDto);
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private String getTokenFromCookies(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
