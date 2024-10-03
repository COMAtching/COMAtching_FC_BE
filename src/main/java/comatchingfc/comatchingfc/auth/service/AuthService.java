package comatchingfc.comatchingfc.auth.service;

import comatchingfc.comatchingfc.auth.dto.UserLoginReq;
import comatchingfc.comatchingfc.auth.enums.TicketType;
import comatchingfc.comatchingfc.auth.jwt.JwtUtil;
import comatchingfc.comatchingfc.auth.jwt.dto.TokenRes;
import comatchingfc.comatchingfc.auth.jwt.refresh.service.RefreshTokenRedisService;
import comatchingfc.comatchingfc.user.entity.UserAiInfo;
import comatchingfc.comatchingfc.user.entity.Users;
import comatchingfc.comatchingfc.user.repository.UserAiInfoRepository;
import comatchingfc.comatchingfc.user.repository.UserRepository;
import comatchingfc.comatchingfc.utils.uuid.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserAiInfoRepository userAiInfoRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRedisService refreshTokenRedisService;

    @Transactional
    public TokenRes userLogin(UserLoginReq userLoginReq) {
        String type = userLoginReq.getType();
        TicketType ticketType = type.equals(TicketType.online.toString()) ? TicketType.online : TicketType.offline;

        if (ticketType.equals(TicketType.online)) {
            // todo: 인증서버에서 예매 번호 확인
        }

        Optional<Users> userOpt = userRepository.findByIdentifyKey(userLoginReq.getTicket());
        String userUuid;
        String userRole;

        if (userOpt.isEmpty()) {
            Users user = Users.builder()
                    .identifyKey(userLoginReq.getTicket())
                    .build();

            UserAiInfo userAiInfo = UserAiInfo.builder()
                    .uuid(UUIDUtil.createUUID())
                    .build();

            userAiInfo.setUsers(user);

            userRepository.save(user);
            userAiInfoRepository.save(userAiInfo);

            userUuid = UUIDUtil.bytesToHex(userAiInfo.getUuid());
            userRole = user.getRole().toString();

        } else {
            Users user = userOpt.get();
            userUuid = UUIDUtil.bytesToHex(user.getUserAiInfo().getUuid());
            userRole = user.getRole().toString();
        }

        String accessToken = jwtUtil.generateAccessToken(userUuid, userRole);
        String refreshToken = refreshTokenRedisService.getRefreshToken(userUuid);

        if (refreshToken == null) {
            refreshToken = jwtUtil.generateRefreshToken(userUuid, userRole);
            refreshTokenRedisService.saveRefreshToken(userUuid, refreshToken);
        }

        return TokenRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
