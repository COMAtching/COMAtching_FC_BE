package comatchingfc.comatchingfc.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import comatchingfc.comatchingfc.admin.dto.AdminLoginReq;
import comatchingfc.comatchingfc.admin.dto.AdminRegisterReq;
import comatchingfc.comatchingfc.admin.service.AdminService;
import comatchingfc.comatchingfc.auth.jwt.JwtUtil;
import comatchingfc.comatchingfc.auth.jwt.dto.TokenRes;
import comatchingfc.comatchingfc.exception.BusinessException;
import comatchingfc.comatchingfc.utils.response.ResponseCode;
import comatchingfc.comatchingfc.utils.security.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private SecurityUtil securityUtil;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("관리자 회원가입 성공 테스트")
    void adminRegisterSuccess() throws Exception {
        //given
        String exAccountId = "admin123";
        String exPassword = "password";
        String exUsername = "AdminUser";

        AdminRegisterReq registerReq = new AdminRegisterReq();
        registerReq.setAccountId(exAccountId);
        registerReq.setPassword(exPassword);
        registerReq.setUsername(exUsername);

        //when
        doNothing().when(adminService).adminRegister(any(AdminRegisterReq.class));

        //then
        mockMvc.perform(post("/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("GEN-000"));

        //verify
        verify(adminService, times(1)).adminRegister(any(AdminRegisterReq.class));
    }

    @Test
    @DisplayName("관리자 ID 중복 확인 - 중복되지 않은 ID일 때")
    void checkAdminDuplicate_NotDuplicate() throws Exception {
        //given
        String accountId = "admin123";

        // Mocking: accountId가 중복되지 않음을 설정
        when(adminService.isAccountDuplicated(accountId)).thenReturn(false);

        //when & then
        mockMvc.perform(get("/admin/check-duplicate/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 응답 상태가 200 OK인지 검증
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 응답 본문 타입이 JSON인지 검증
                .andExpect(content().string("true")); // 중복되지 않은 ID이므로 true 반환

        //verify
        verify(adminService, times(1)).isAccountDuplicated(accountId);
    }

    @Test
    @DisplayName("관리자 ID 중복 확인 - 중복된 경우")
    void checkAdminDuplicate_Duplicate() throws Exception {
        //given
        String accountId = "admin123";

        // Mocking: adminService.isAccountDuplicated(accountId)가 true를 반환하도록 설정
        when(adminService.isAccountDuplicated(accountId)).thenReturn(true);

        //when & then
        mockMvc.perform(get("/admin/check-duplicate/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 응답 상태가 200 OK인지 검증
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 응답 본문 타입이 JSON인지 검증
                .andExpect(content().string("false")); // 중복된 ID이므로 false 반환

        //verify
        verify(adminService, times(1)).isAccountDuplicated(accountId);
    }

    @Test
    @DisplayName("관리자 로그인 성공 테스트")
    void adminLoginSuccess() throws Exception {
        //given
        String exAccountId = "admin123";
        String exPassword = "password";

        AdminLoginReq loginReq = new AdminLoginReq();
        loginReq.setAccountId(exAccountId);
        loginReq.setPassword(exPassword);

        // Mocking: adminService.adminLogin를 호출하면 TokenRes 객체를 반환하도록 설정
        TokenRes tokenRes = new TokenRes("accessToken123", "refreshToken123");
        when(adminService.adminLogin(any(AdminLoginReq.class))).thenReturn(tokenRes);

        // Mocking: SecurityUtil의 setAccessResponseCookie와 setRefreshResponseCookie 메소드가 호출되면, 해당 쿠키를 반환하도록 설정
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "accessToken123")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60) // 1시간
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "refreshToken123")
                .httpOnly(true)
                .secure(false)
                .path("/auth/refresh")
                .maxAge(24 * 60 * 60) // 1일
                .sameSite("Strict")
                .build();

        when(securityUtil.setAccessResponseCookie("accessToken123")).thenReturn(accessCookie);
        when(securityUtil.setRefreshResponseCookie("refreshToken123")).thenReturn(refreshCookie);

        //when & then
        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq))) // 요청 본문을 JSON으로 직렬화
                .andExpect(status().isOk()) // 응답 상태가 200 OK인지 검증
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 응답 본문 타입이 JSON인지 검증
                .andExpect(jsonPath("$.status").value(200)) // 커스텀 Response 객체의 status 필드가 200인지 검증
                .andExpect(jsonPath("$.code").value("GEN-000")) // 커스텀 Response 객체의 code 필드가 "GEN-000"인지 검증
                // 쿠키 검증
                .andExpect(cookie().exists("accessToken")) // accessToken 쿠키가 존재하는지 검증
                .andExpect(cookie().value("accessToken", "accessToken123")) // accessToken 쿠키의 값이 올바른지 검증
                .andExpect(cookie().httpOnly("accessToken", true)) // accessToken 쿠키가 HttpOnly인지 검증
                .andExpect(cookie().secure("accessToken", false)) // accessToken 쿠키가 Secure 플래그를 가지고 있는지 검증
                .andExpect(cookie().path("accessToken", "/")) // accessToken 쿠키의 경로가 올바른지 검증
                .andExpect(cookie().maxAge("accessToken", 60 * 60)) // accessToken 쿠키의 maxAge가 올바른지 검증
                .andExpect(cookie().sameSite("accessToken", "Strict")) // accessToken 쿠키의 SameSite 속성이 올바른지 검증

                .andExpect(cookie().exists("refreshToken")) // refreshToken 쿠키가 존재하는지 검증
                .andExpect(cookie().value("refreshToken", "refreshToken123")) // refreshToken 쿠키의 값이 올바른지 검증
                .andExpect(cookie().httpOnly("refreshToken", true)) // refreshToken 쿠키가 HttpOnly인지 검증
                .andExpect(cookie().secure("refreshToken", false)) // refreshToken 쿠키가 Secure 플래그를 가지고 있는지 검증
                .andExpect(cookie().path("refreshToken", "/auth/refresh")) // refreshToken 쿠키의 경로가 올바른지 검증
                .andExpect(cookie().maxAge("refreshToken", 24 * 60 * 60)) // refreshToken 쿠키의 maxAge가 올바른지 검증
                .andExpect(cookie().sameSite("refreshToken", "Strict")); // refreshToken 쿠키의 SameSite 속성이 올바른지 검증

        //verify
        verify(adminService, times(1)).adminLogin(any(AdminLoginReq.class));
        verify(securityUtil, times(1)).setAccessResponseCookie("accessToken123");
        verify(securityUtil, times(1)).setRefreshResponseCookie("refreshToken123");
    }

    @Test
    @DisplayName("관리자 로그인 실패 - 잘못된 ID")
    void adminLoginFail_InvalidAccountId() throws Exception {
        //given
        String exAccountId = "nonexistent";
        String exPassword = "password";

        AdminLoginReq loginReq = new AdminLoginReq();
        loginReq.setAccountId(exAccountId);
        loginReq.setPassword(exPassword);

        // Mocking: adminService.adminLogin를 호출하면 BusinessException을 던지도록 설정
        doThrow(new BusinessException(ResponseCode.INVALID_LOGIN)).when(adminService).adminLogin(any(AdminLoginReq.class));

        //when & then
        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 응답 본문 타입이 JSON인지 검증
                .andExpect(jsonPath("$.status").value(ResponseCode.INVALID_LOGIN.getStatus())) // 커스텀 Response 객체의 status 필드가 INVALID_LOGIN 상태 코드인지 검증
                .andExpect(jsonPath("$.code").value(ResponseCode.INVALID_LOGIN.getCode())); // 커스텀 Response 객체의 code 필드가 INVALID_LOGIN 코드인지 검증

        //verify
        verify(adminService, times(1)).adminLogin(any(AdminLoginReq.class));
        verify(securityUtil, never()).setAccessResponseCookie(anyString());
        verify(securityUtil, never()).setRefreshResponseCookie(anyString());
    }

}
