package comatchingfc.comatchingfc.admin.service;

import comatchingfc.comatchingfc.admin.dto.AdminLoginReq;
import comatchingfc.comatchingfc.admin.dto.AdminRegisterReq;
import comatchingfc.comatchingfc.admin.entity.Admin;
import comatchingfc.comatchingfc.admin.repository.AdminRepository;
import comatchingfc.comatchingfc.auth.jwt.JwtUtil;
import comatchingfc.comatchingfc.auth.jwt.dto.TokenRes;
import comatchingfc.comatchingfc.auth.jwt.refresh.service.RefreshTokenService;
import comatchingfc.comatchingfc.exception.BusinessException;
import comatchingfc.comatchingfc.utils.response.ResponseCode;
import comatchingfc.comatchingfc.utils.uuid.UUIDUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AdminService adminService;

    @Captor
    ArgumentCaptor<Admin> adminCaptor;

    @Test
    @DisplayName("관리자 회원가입 성공 테스트")
    void adminRegisterSuccess() {
        //given
        String exAccountId = "admin123";
        String exPassword = "password";
        String exUsername = "AdminUser";

        AdminRegisterReq registerReq = new AdminRegisterReq();
        registerReq.setAccountId(exAccountId);
        registerReq.setPassword(exPassword);
        registerReq.setUsername(exUsername);

        // Mocking: accountId가 중복되지 않음을 설정
        when(adminRepository.existsAdminByAccountId(exAccountId)).thenReturn(false);
        // Mocking: 비밀번호 암호화 결과 설정
        when(passwordEncoder.encode(exPassword)).thenReturn("encodedPassword");
        // Mocking: Admin 객체 저장 시 반환될 객체 설정
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        assertThatCode(() -> adminService.adminRegister(registerReq))
                .doesNotThrowAnyException();

        //then
        // adminRepository.existsAdminByAccountId가 정확히 한 번 호출되었는지 검증
        verify(adminRepository, times(1)).existsAdminByAccountId(exAccountId);

        // passwordEncoder.encode가 정확히 한 번 호출되었는지 검증
        verify(passwordEncoder, times(1)).encode(exPassword);

        // adminRepository.save가 정확히 한 번 호출되었는지 검증 및 저장된 Admin 객체 캡처
        verify(adminRepository, times(1)).save(adminCaptor.capture());

        // 캡처된 Admin 객체의 필드 검증
        Admin savedAdmin = adminCaptor.getValue();
        assertThat(savedAdmin.getUuid()).isNotNull();
        assertThat(savedAdmin.getUsername()).isEqualTo(exUsername);
        assertThat(savedAdmin.getAccountId()).isEqualTo(exAccountId);
        assertThat(savedAdmin.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("중복된 관리자 id로 가입 테스트")
    void adminRegister_DuplicateAccountId_ShouldThrowException() {
        //given
        String exAccountId = "admin123";
        String exPassword = "password";
        String exUsername = "AdminUser";

        AdminRegisterReq registerReq = new AdminRegisterReq();
        registerReq.setAccountId(exAccountId);
        registerReq.setPassword(exPassword);
        registerReq.setUsername(exUsername);

        // Mocking: accountId가 중복됨을 설정
        when(adminRepository.existsAdminByAccountId("admin123")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> adminService.adminRegister(registerReq))
                .isInstanceOf(BusinessException.class)
                .extracting("responseCode")
                .isEqualTo(ResponseCode.ACCOUNT_ID_DUPLICATED);

        // adminRepository.existsAdminByAccountId가 정확히 한 번 호출되었는지 검증
        verify(adminRepository, times(1)).existsAdminByAccountId("admin123");

        // passwordEncoder.encode가 호출되지 않았는지 검증
        verify(passwordEncoder, never()).encode(anyString());

        // adminRepository.save가 호출되지 않았는지 검증
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    @DisplayName("ID 중복확인 테스트")
    void isAccountDuplicatedTrueTest() {
        //given
        String accountId = "admin123";
        when(adminRepository.existsAdminByAccountId(accountId)).thenReturn(true);
        //when
        Boolean result = adminService.isAccountDuplicated(accountId);
        //then
        assertThat(result).isTrue();
        verify(adminRepository, times(1)).existsAdminByAccountId(accountId);
    }

    @Test
    @DisplayName("ID 중복확인 테스트")
    void isAccountDuplicatedFalseTest() {
        //given
        String accountId = "admin123";
        when(adminRepository.existsAdminByAccountId(accountId)).thenReturn(false);
        //when
        Boolean result = adminService.isAccountDuplicated(accountId);
        //then
        assertThat(result).isFalse();
        verify(adminRepository, times(1)).existsAdminByAccountId(accountId);
    }

    @Test
    @DisplayName("관리자 로그인 성공 테스트")
    void adminLogin_Success() {
        // Arrange
        String exAccountId = "admin123";
        String exPassword = "password";
        String exEncodedPassword = "encodedPassword";
        String exUsername = "AdminUser";

        AdminLoginReq loginReq = new AdminLoginReq();
        loginReq.setAccountId(exAccountId);
        loginReq.setPassword(exPassword);

        Admin admin = Admin.builder()
                .uuid(UUIDUtil.createUUID())
                .accountId(exAccountId)
                .password(exEncodedPassword)
                .username(exUsername)
                .build();

        // Mocking: adminRepository.findByAccountId가 Admin 객체를 반환하도록 설정
        when(adminRepository.findByAccountId(exAccountId)).thenReturn(Optional.of(admin));
        // Mocking: 비밀번호가 일치함을 설정
        when(passwordEncoder.matches(exPassword, exEncodedPassword)).thenReturn(true);
        // Admin UUID를 Hex 문자열로 변환
        String adminUuidHex = UUIDUtil.bytesToHex(admin.getUuid());
        // Mocking: accessToken과 refreshToken 생성
        String accessToken = "accessToken123";
        String refreshToken = "refreshToken123";

        when(jwtUtil.generateAccessToken(adminUuidHex, "ROLE_ADMIN")).thenReturn(accessToken);
        when(refreshTokenService.getRefreshToken(adminUuidHex)).thenReturn(null); // 기존 refreshToken 없음
        when(jwtUtil.generateRefreshToken(adminUuidHex, "ROLE_ADMIN")).thenReturn(refreshToken);

        // Act
        TokenRes tokenRes = adminService.adminLogin(loginReq);

        // Assert
        assertThat(tokenRes).isNotNull();
        assertThat(tokenRes.getAccessToken()).isEqualTo(accessToken);
        assertThat(tokenRes.getRefreshToken()).isEqualTo(refreshToken);

        // verify 호출 검증
        verify(adminRepository, times(1)).findByAccountId(exAccountId);
        verify(passwordEncoder, times(1)).matches(exPassword, exEncodedPassword);
        verify(jwtUtil, times(1)).generateAccessToken(adminUuidHex, "ROLE_ADMIN");
        verify(refreshTokenService, times(1)).getRefreshToken(adminUuidHex);
        verify(refreshTokenService, times(1)).saveRefreshToken(adminUuidHex, refreshToken);
        verify(jwtUtil, times(1)).generateRefreshToken(adminUuidHex, "ROLE_ADMIN");
    }

    @Test
    @DisplayName("관리자 로그인 잘못된 ID")
    void adminLoginFail_AccountId() {
        // Arrange
        AdminLoginReq loginReq = new AdminLoginReq();
        loginReq.setAccountId("nonexistent");
        loginReq.setPassword("password");

        // Mocking: adminRepository.findByAccountId가 빈 Optional을 반환하도록 설정
        when(adminRepository.findByAccountId("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminService.adminLogin(loginReq))
                .isInstanceOf(BusinessException.class)
                .extracting("responseCode")
                .isEqualTo(ResponseCode.INVALID_LOGIN);

        // verify 호출 검증
        verify(adminRepository, times(1)).findByAccountId("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateAccessToken(anyString(), anyString());
        verify(refreshTokenService, never()).getRefreshToken(anyString());
    }

    @Test
    @DisplayName("관리자 로그인 잘못된 PW")
    void adminLoginFail_Password() {
        // Arrange
        AdminLoginReq loginReq = new AdminLoginReq();
        loginReq.setAccountId("admin123");
        loginReq.setPassword("wrongpassword");

        Admin admin = Admin.builder()
                .uuid(UUIDUtil.createUUID())
                .accountId("admin123")
                .password("encodedPassword")
                .username("AdminUser")
                .build();

        // Mocking: adminRepository.findByAccountId가 Admin 객체를 반환하도록 설정
        when(adminRepository.findByAccountId("admin123")).thenReturn(Optional.of(admin));

        // Mocking: 비밀번호가 일치하지 않음을 설정
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> adminService.adminLogin(loginReq))
                .isInstanceOf(BusinessException.class)
                .extracting("responseCode")
                .isEqualTo(ResponseCode.INVALID_LOGIN);

        // verify 호출 검증
        verify(adminRepository, times(1)).findByAccountId("admin123");
        verify(passwordEncoder, times(1)).matches("wrongpassword", "encodedPassword");
        verify(jwtUtil, never()).generateAccessToken(anyString(), anyString());
        verify(refreshTokenService, never()).getRefreshToken(anyString());
    }
}
