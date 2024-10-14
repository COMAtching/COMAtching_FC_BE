package comatchingfc.comatchingfc.auth.jwt;

import comatchingfc.comatchingfc.utils.response.Response;
import comatchingfc.comatchingfc.utils.response.ResponseCode;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, e);
        }
    }

    private void setErrorResponse(HttpStatus status, HttpServletResponse response, Throwable ex) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json; charset=UTF-8");

        Response<?> res;
        if ("TOKEN_EXPIRED".equals(ex.getMessage())) {
            res = Response.errorResponse(ResponseCode.ACCESS_TOKEN_EXPIRED);
            log.info("[JwtExceptionFilter] - ACCESS_TOKEN_EXPIRE");
        } else if ("REFRESH_TOKEN_EXPIRED".equals(ex.getMessage())) {
            res = Response.errorResponse(ResponseCode.REFRESH_TOKEN_EXPIRED);
            log.info("[JwtExceptionFilter] - REFRESH_TOKEN_EXPIRE");
        } else if ("TOKEN_INVALID".equals(ex.getMessage())) {
            res = Response.errorResponse(ResponseCode.JWT_ERROR);
            log.info("[JwtExceptionFilter] - INVALID");
        }
        else {
            res = Response.errorResponse(ResponseCode.JWT_ERROR);
            log.info("[JwtExceptionFilter] - GENERAL_ERROR");
        }

        String resJson = res.convertToJson();
        response.getWriter().write(resJson);
        log.info("[JwtExceptionFilter]: {}", resJson);
    }
}
