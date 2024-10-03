package comatchingfc.comatchingfc.utils.security;

import comatchingfc.comatchingfc.admin.entity.Admin;
import comatchingfc.comatchingfc.admin.repository.AdminRepository;
import comatchingfc.comatchingfc.auth.oauth2.dto.CustomOAuth2User;
import comatchingfc.comatchingfc.exception.BusinessException;
import comatchingfc.comatchingfc.user.entity.Users;
import comatchingfc.comatchingfc.user.repository.UserRepository;
import comatchingfc.comatchingfc.utils.response.ResponseCode;
import comatchingfc.comatchingfc.utils.uuid.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    /**
     * 현재 인증된 일반 사용자 정보를 반환
     *
     * @return 현재 인증된 User 엔티티
     */
    public Users getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
            String uuid = customOAuth2User.getName();
            String role = customOAuth2User.getRole();

            if ("ROLE_ADMIN".equals(role)) {
                throw new BusinessException(ResponseCode.ACCESS_DENIED);
            }

            return userRepository.findUsersByUuid(UUIDUtil.uuidStringToBytes(uuid))
                    .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND));
        }

        throw new BusinessException(ResponseCode.USER_NOT_FOUND);
    }

    /**
     * 현재 인증된 Admin 사용자 정보를 반환
     *
     * @return 현재 인증된 Admin 엔티티
     */
    public Admin getCurrentAdminEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
            String uuid = customOAuth2User.getName();
            String role = customOAuth2User.getRole();

            if (!"ROLE_ADMIN".equals(role)) {
                throw new BusinessException(ResponseCode.ACCESS_DENIED);
            }

            return adminRepository.findByUuid(UUIDUtil.uuidStringToBytes(uuid))
                    .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND));
        }

        throw new BusinessException(ResponseCode.USER_NOT_FOUND);
    }


    public ResponseCookie setRefreshResponseCookie(String refreshToken) {
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/auth/refresh")
                .maxAge(24 * 60 * 60) // 1일
                .sameSite("Strict")
                .build();
        return refreshCookie;
    }

    public ResponseCookie setAccessResponseCookie(String accessToken) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false) // HTTPS 환경에서만 전송하려면 true
                .path("/")
                .maxAge(60 * 60) // 1시간
                .sameSite("Strict")
                .build();
        return accessCookie;
    }
}
