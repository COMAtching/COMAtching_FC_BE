package comatchingfc.comatchingfc.auth.controller;

import comatchingfc.comatchingfc.auth.dto.UserLoginReq;
import comatchingfc.comatchingfc.auth.jwt.dto.TokenRes;
import comatchingfc.comatchingfc.auth.service.AuthService;
import comatchingfc.comatchingfc.utils.response.Response;
import comatchingfc.comatchingfc.utils.security.SecurityUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityUtil securityUtil;

    @PostMapping("/user/login")
    public Response<Void> userLogin(@RequestBody UserLoginReq userLoginReq, HttpServletResponse response) {

        TokenRes tokenRes = authService.userLogin(userLoginReq);

        // 쿠키에 토큰 저장
        response.addHeader("Set-Cookie", securityUtil.setAccessResponseCookie(tokenRes.getAccessToken()).toString());
        response.addHeader("Set-Cookie", securityUtil.setRefreshResponseCookie(tokenRes.getRefreshToken()).toString());

        return Response.ok();
    }
}
