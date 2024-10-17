package comatchingfc.comatchingfc.utils.rabbitMQ;

import java.util.List;
import java.util.UUID;

import comatchingfc.comatchingfc.user.entity.CheerPropensity;
import comatchingfc.comatchingfc.user.entity.UserAiInfo;
import comatchingfc.comatchingfc.user.entity.UserFeature;
import comatchingfc.comatchingfc.user.entity.Users;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import comatchingfc.comatchingfc.exception.BusinessException;
 import comatchingfc.comatchingfc.user.enums.UserCrudType;
import comatchingfc.comatchingfc.utils.rabbitMQ.Message.req.MatchReqMsg;
import comatchingfc.comatchingfc.utils.rabbitMQ.Message.req.UserCrudReqMsg;
import comatchingfc.comatchingfc.utils.rabbitMQ.Message.res.MatchResMsg;
import comatchingfc.comatchingfc.utils.rabbitMQ.Message.res.UserCrudResMsg;
import comatchingfc.comatchingfc.utils.response.RabbitMQStateCode;
import comatchingfc.comatchingfc.utils.response.ResponseCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserRabbitMQUtil {
	private final RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.routing-keys.user-crud-request}")
	private String matchRequestQueue;

	public UserRabbitMQUtil(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate = rabbitTemplate;
	}

	/**
	 * rabbitMQ 매칭 요청
	 * @param requestMsg : 매칭 요청 Dto
	 * @return 매칭 결과
	 */
	public UserCrudResMsg requestUserToCsv(UserCrudReqMsg requestMsg, UserCrudType userCrudType){
		requestMsg.addType(userCrudType);
		String requestId = UUID.randomUUID().toString();
		CorrelationData correlationData = new CorrelationData(requestId);
		ParameterizedTypeReference<UserCrudResMsg> responseType = new ParameterizedTypeReference<UserCrudResMsg>(){};

		log.info("[UserRabbitMQUtil requestUserToCsv] request={}", requestMsg.toJsonString());
		UserCrudResMsg responseMsg =  rabbitTemplate.convertSendAndReceiveAsType(
			matchRequestQueue,
			requestMsg,
			(MessagePostProcessor) null,
			correlationData,
			responseType);

		String stateCode = responseMsg.getStateCode();
		log.info("[UserRabbitMQUtil requestUserToCsv] response={}", responseMsg.toJsonString());
		if(responseMsg == null){
			throw new BusinessException(ResponseCode.MATCH_GENERAL_FAIL);
		}

		if(!stateCode.equals(RabbitMQStateCode.CRUD_SUCCESS.getCode())){
			log.info("[MatchingRabbitMQUtil requestMatch] User Crud Exception - stateCode = {}, message = {}", responseMsg.getStateCode(), responseMsg.getMessage());
		}

		log.info("[MatchingRabbitMQUtil requestMatch] stateCode = {}", responseMsg.getStateCode());

		return responseMsg;
	}

	public UserCrudReqMsg getUserCrudReqMsg(Users user) {
		UserAiInfo userAiInfo = user.getUserAiInfo();
		UserFeature userFeature = user.getUserAiInfo().getUserFeature();
		List<CheerPropensity> cheerPropensities = userFeature.getCheerPropensities();

		return new UserCrudReqMsg(userFeature, cheerPropensities, userAiInfo);
	}
}
