package comatchingfc.comatchingfc.match.service;

import static comatchingfc.comatchingfc.user.enums.Gender.*;
import static comatchingfc.comatchingfc.user.enums.UserCrudType.*;

import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import comatchingfc.comatchingfc.exception.BusinessException;
import comatchingfc.comatchingfc.match.dto.req.MatchReq;
import comatchingfc.comatchingfc.match.dto.res.MatchRes;
import comatchingfc.comatchingfc.match.entity.MatchingHistory;
import comatchingfc.comatchingfc.match.repository.MatchingHistoryRepository;
import comatchingfc.comatchingfc.user.entity.CheerPropensity;
import comatchingfc.comatchingfc.user.entity.UserAiInfo;
import comatchingfc.comatchingfc.user.entity.UserFeature;
import comatchingfc.comatchingfc.user.entity.Users;
import comatchingfc.comatchingfc.user.enums.CheerPropensityEnum;
import comatchingfc.comatchingfc.user.enums.Gender;
import comatchingfc.comatchingfc.user.repository.UserAiInfoRepository;
import comatchingfc.comatchingfc.user.repository.UserFeatureRepository;
import comatchingfc.comatchingfc.user.repository.UserRepository;
import comatchingfc.comatchingfc.utils.rabbitMQ.MatchingRabbitMQUtil;
import comatchingfc.comatchingfc.utils.rabbitMQ.Message.req.MatchReqMsg;
import comatchingfc.comatchingfc.utils.rabbitMQ.Message.req.UserCrudReqMsg;
import comatchingfc.comatchingfc.utils.rabbitMQ.Message.res.MatchResMsg;
import comatchingfc.comatchingfc.utils.rabbitMQ.UserRabbitMQUtil;
import comatchingfc.comatchingfc.utils.response.ResponseCode;
import comatchingfc.comatchingfc.utils.security.SecurityUtil;
import comatchingfc.comatchingfc.utils.uuid.UUIDUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {
	private final UserRepository userRepository;
	private final UserFeatureRepository userFeatureRepository;
	private final UserAiInfoRepository userAiInfoRepository;
	private final MatchingHistoryRepository matchingHistoryRepository;
	private final MatchingRabbitMQUtil matchingRabbitMQUtil;
	private final UserRabbitMQUtil userRabbitMQUtil;
	private final SecurityUtil securityUtil;
	private final RedisTemplate redisTemplate;

	/**
	 * 매칭 프로세스 및 matching history 생성
	 * @param matchReq 이성 옵션
	 * @return 매칭 결과
	 */
	@Transactional
	public MatchRes requestMatch(MatchReq matchReq){
		Users applier = securityUtil.getCurrentUserEntity();

		if(applier.getMatchingHistory() != null){
			throw new BusinessException(ResponseCode.MATCH_ALREADY_EXIST);
		}

		UserAiInfo applierAiInfo = applier.getUserAiInfo();
		UserFeature applierFeature = applierAiInfo.getUserFeature();
		List<CheerPropensity> applierCheerPropensities = applierFeature.getCheerPropensities();

		boolean lackOfResource = checkLackOfResource(matchReq.getGenderOption(), applierFeature.getAge(), applierFeature.getPropensity());

		MatchReqMsg matchReqMsg = new MatchReqMsg(applierFeature, applierAiInfo, applierCheerPropensities, matchReq.getGenderOption());
		MatchResMsg matchResMsg = matchingRabbitMQUtil.requestMatch(matchReqMsg);

		UserAiInfo enemyAiInfo = userAiInfoRepository.findByUuid(UUIDUtil.stringToByteLiteral(matchResMsg.getEnemyUuid()))
			.orElseThrow(() -> new BusinessException(ResponseCode.ENEMY_NOT_FOUND));

		Users enemy = enemyAiInfo.getUsers();
		UserFeature enemyFeature = enemyAiInfo.getUserFeature();
		List<CheerPropensity> enemyCheerPropensities = enemyFeature.getCheerPropensities();

		UserCrudReqMsg userCrudReqMsg = new UserCrudReqMsg(enemyFeature, enemyCheerPropensities, enemyAiInfo);
		userRabbitMQUtil.requestUserToCsv(userCrudReqMsg, DELETE);

		applierAiInfo.updateIsPick(true);
		enemyAiInfo.updateIsPicked(true);

		MatchingHistory matchingHistory = MatchingHistory.builder()
			.applier(applier)
			.enemy(enemyAiInfo.getUsers())
			.build();

		applier.addMatchingHistory(matchingHistory);

		saveMatchingHistoryToCache(matchingHistory);

		matchingHistoryRepository.save(matchingHistory);

		return MatchRes.builder()
			.age(enemyFeature.getAge())
			.gender(enemyFeature.getGender())
			.username(enemy.getUsername())
			.propensity(enemyFeature.getPropensity())
			.cheeringPlayer(enemy.getCheeringPlayer())
			.socialId(enemy.getSocialId())
			.lackOfResource(lackOfResource)
			.build();
	}

	private boolean checkLackOfResource(Gender genderOption, int age, CheerPropensityEnum propensityOption){
		long count = 0;
		if(genderOption.equals(RANDOM)){
			count = userFeatureRepository.countMatchableUserAndPropensityAndAge(propensityOption, age);
			log.info("[MatchService - checkLackOfResource] : count={}", count);
		}
		else{
			count = userFeatureRepository.countMatchableUserByGenderAndPropensityAndAge(genderOption,
				propensityOption, age);
			log.info("[MatchService - checkLackOfResource] : count={}", count);
		}
		return (count == 0)? true : false;
	}

	@CachePut(value = "matchingHistory", key = "#matchingHistory.applier.id")
	public MatchingHistory saveMatchingHistoryToCache(MatchingHistory matchingHistory) {
		return matchingHistory;
	}

}
