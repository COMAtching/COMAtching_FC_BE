package comatchingfc.comatchingfc.admin.service;

import comatchingfc.comatchingfc.admin.dto.AdminLoginReq;
import comatchingfc.comatchingfc.admin.dto.AdminRegisterReq;
import comatchingfc.comatchingfc.admin.entity.Admin;
import comatchingfc.comatchingfc.admin.repository.AdminRepository;
import comatchingfc.comatchingfc.auth.jwt.JwtUtil;
import comatchingfc.comatchingfc.auth.jwt.dto.TokenRes;
import comatchingfc.comatchingfc.auth.jwt.refresh.service.RefreshTokenRedisService;
import comatchingfc.comatchingfc.exception.BusinessException;
import comatchingfc.comatchingfc.user.entity.Users;
import comatchingfc.comatchingfc.user.repository.UserRepository;
import comatchingfc.comatchingfc.utils.response.ResponseCode;
import comatchingfc.comatchingfc.utils.uuid.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRedisService refreshTokenRedisService;

    @Transactional
    public void adminRegister(AdminRegisterReq registerReq) {

        Boolean exist = adminRepository.existsAdminByAccountId(registerReq.getAccountId());
        if (exist) {
            throw new BusinessException(ResponseCode.ACCOUNT_ID_DUPLICATED);
        }

        String encryptedPassword = passwordEncoder.encode(registerReq.getPassword());

        Admin admin = Admin.builder()
                .uuid(UUIDUtil.createUUID())
                .username(registerReq.getUsername())
                .accountId(registerReq.getAccountId())
                .password(encryptedPassword)
                .build();

        adminRepository.save(admin);
    }

    /**
     * 계정 중복확인
     * @param accountId 계정ID
     * @return 중복이면 true
     */
    public Boolean isAccountDuplicated(String accountId) {
        return adminRepository.existsAdminByAccountId(accountId);
    }

    @Transactional
    public TokenRes adminLogin(AdminLoginReq loginReq) {
        Admin admin = adminRepository.findByAccountId(loginReq.getAccountId())
                .orElseThrow(() -> new BusinessException(ResponseCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(loginReq.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResponseCode.INVALID_LOGIN);
        }

        String adminUuid = UUIDUtil.bytesToHex(admin.getUuid());
        String accessToken = jwtUtil.generateAccessToken(adminUuid, "ROLE_ADMIN");
        String refreshToken = refreshTokenRedisService.getRefreshToken(adminUuid);

        if (refreshToken == null) {
            refreshToken = jwtUtil.generateRefreshToken(adminUuid, "ROLE_ADMIN");
            refreshTokenRedisService.saveRefreshToken(adminUuid, refreshToken);
        }

        return TokenRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void activateUser(String ticket) {
        Users user = userRepository.findByIdentifyKey(ticket)
                .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND));

        user.activateUser();
    }
}
