package comatchingfc.comatchingfc.user.service;

import comatchingfc.comatchingfc.auth.jwt.refresh.service.RefreshTokenRedisService;
import comatchingfc.comatchingfc.user.dto.req.FeatureReq;
import comatchingfc.comatchingfc.user.dto.UserInfo;
import comatchingfc.comatchingfc.user.entity.CheerPropensity;
import comatchingfc.comatchingfc.user.entity.UserFeature;
import comatchingfc.comatchingfc.user.entity.Users;
import comatchingfc.comatchingfc.user.enums.Gender;
import comatchingfc.comatchingfc.user.enums.UserCrudType;
import comatchingfc.comatchingfc.user.repository.UserRepository;
import comatchingfc.comatchingfc.utils.rabbitMQ.Message.req.UserCrudReqMsg;
import comatchingfc.comatchingfc.utils.rabbitMQ.UserRabbitMQUtil;
import comatchingfc.comatchingfc.utils.security.SecurityUtil;
import comatchingfc.comatchingfc.utils.uuid.UUIDUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SecurityUtil securityUtil;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final UserRepository userRepository;
    private final UserRabbitMQUtil userRabbitMQUtil;

    public Long getParticipations() {
        return userRepository.count();
    }

    @Transactional
    public void updateFeature(FeatureReq featureReq) {
        Users user = securityUtil.getCurrentUserEntity();

        Gender gender = featureReq.getGender().equals(Gender.MALE.toString()) ? Gender.MALE : Gender.FEMALE;

        user.updateUsername(featureReq.getUsername());
        user.getUserAiInfo().getUserFeature().updateAge(featureReq.getAge());
        user.getUserAiInfo().getUserFeature().updateGender(gender);
        user.updateSocialId(featureReq.getSocialId());
        user.updateCheeringPlayer(featureReq.getCheeringPlayer());
    }

    public UserInfo getUserInfo() {
        Users user = securityUtil.getCurrentUserEntity();
        UserFeature userFeature = user.getUserAiInfo().getUserFeature();
        List<CheerPropensity> cheerPropensities = userFeature.getCheerPropensities();

        return UserInfo.builder()
                .username(user.getUsername())
                .age(userFeature.getAge())
                .gender(userFeature.getGender())
                .socialId(user.getSocialId())
                .cheeringPlayer(user.getCheeringPlayer())
                .cheerPropensity(userFeature.getPropensity())
                .passionType(cheerPropensities.get(0).getScore())
                .focusType(cheerPropensities.get(1).getScore())
                .soccerNoviceType(cheerPropensities.get(2).getScore())
                .soccerExpertType(cheerPropensities.get(3).getScore())
                .mukbangType(cheerPropensities.get(4).getScore())
                .socialType(cheerPropensities.get(5).getScore())
                .build();
    }

    public Boolean userMatched() {
        Users user = securityUtil.getCurrentUserEntity();
        return user.getUserAiInfo().isPick();
    }

    public UserInfo getEnemyUserInfo() {
        Users user = securityUtil.getCurrentUserEntity();
        Users enemy = user.getMatchingHistory().getEnemy();
        UserFeature userFeature = enemy.getUserAiInfo().getUserFeature();
        List<CheerPropensity> cheerPropensities = userFeature.getCheerPropensities();

        return UserInfo.builder()
                .username(enemy.getUsername())
                .age(userFeature.getAge())
                .gender(userFeature.getGender())
                .socialId(enemy.getSocialId())
                .cheeringPlayer(enemy.getCheeringPlayer())
                .cheerPropensity(userFeature.getPropensity())
                .passionType(cheerPropensities.get(0).getScore())
                .focusType(cheerPropensities.get(1).getScore())
                .soccerNoviceType(cheerPropensities.get(2).getScore())
                .soccerExpertType(cheerPropensities.get(3).getScore())
                .mukbangType(cheerPropensities.get(4).getScore())
                .socialType(cheerPropensities.get(5).getScore())
                .build();
    }

    /**
     * 유저 비활성화
     */
    @Transactional
    public void deactivateUser() {
        Users user = securityUtil.getCurrentUserEntity();
        UserCrudReqMsg userCrudReqMsg = userRabbitMQUtil.getUserCrudReqMsg(user);

        userRabbitMQUtil.requestUserToCsv(userCrudReqMsg, UserCrudType.DELETE);
        user.deactivateUser();

        String userUuid = UUIDUtil.bytesToHex(user.getUserAiInfo().getUuid());
        refreshTokenRedisService.deleteRefreshToken(userUuid);
    }

    @Transactional
    public void logout() {
        Users user = securityUtil.getCurrentUserEntity();
        String userUuid = UUIDUtil.bytesToHex(user.getUserAiInfo().getUuid());
        refreshTokenRedisService.deleteRefreshToken(userUuid);
    }
}
