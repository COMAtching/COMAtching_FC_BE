package comatchingfc.comatchingfc.utils.rabbitMQ;

import java.util.UUID;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import comatchingfc.comatchingfc.exception.BusinessException;
import comatchingfc.comatchingfc.user.entity.CheerPropensity;
import comatchingfc.comatchingfc.utils.rabbitMQ.Message.req.MatchReqMsg;
import comatchingfc.comatchingfc.utils.rabbitMQ.Message.res.MatchResMsg;
import comatchingfc.comatchingfc.utils.response.RabbitMQStateCode;
import comatchingfc.comatchingfc.utils.response.ResponseCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MatchingRabbitMQUtil {
	private final RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.routing-keys.match-request}")
	private String matchRequestQueue;

	public MatchingRabbitMQUtil(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate = rabbitTemplate;
	}

	/**
	 * rabbitMQ 매칭 요청
	 * @param requestMsg : 매칭 요청 Dto
	 * @return 매칭 결과
	 */
	public MatchResMsg requestMatch(MatchReqMsg requestMsg){
		String requestId = UUID.randomUUID().toString();
		CorrelationData correlationData = new CorrelationData(requestId);
		ParameterizedTypeReference<MatchResMsg> responseType = new ParameterizedTypeReference<MatchResMsg>(){};

		log.info("[MatchingRabbitMQUtil requestMatch] request={}", requestMsg.toJsonString());
		MatchResMsg responseMsg =  rabbitTemplate.convertSendAndReceiveAsType(
			matchRequestQueue,
			requestMsg,
			(MessagePostProcessor) null,
			correlationData,
			responseType);

		String stateCode = responseMsg.getStateCode();

		if(responseMsg == null){
			throw new BusinessException(ResponseCode.MATCH_GENERAL_FAIL);
		}

		if(!stateCode.equals(RabbitMQStateCode.MATCH_SUCCESS.getCode())){
			log.info("[MatchingRabbitMQUtil requestMatch] match request Exception - stateCode = {}, message={}", stateCode, responseMsg.getMessage());
		}

		log.info("[MatchingRabbitMQUtil requestMatch] stateCode = {} / responseMsg.getEnemyUuid={} ", responseMsg.getStateCode(), responseMsg.getEnemyUuid());
		return responseMsg;
	}
}
