package comatchingfc.comatchingfc.utils.rabbitMQ;

import java.util.UUID;

import comatchingfc.comatchingfc.exception.BusinessException;
import comatchingfc.comatchingfc.utils.response.RabbitMQStateCode;
import comatchingfc.comatchingfc.utils.response.ResponseCode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import comatchingfc.comatchingfc.utils.rabbitMQ.Message.req.ReserveAuthReqMsg;
import comatchingfc.comatchingfc.utils.rabbitMQ.Message.res.ReserveAuthResMsg;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthRabbitMQUtil {

	private final RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.routing-keys.reserve-auth}")
	private String matchRequestQueue;

	public AuthRabbitMQUtil(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate = rabbitTemplate;
	}

	/**
	 * 예매 번호 인증 요청
	 * @param reserveNumber : 예매번호
	 * @return : boolean - 인증 성공 여부
	 */
	public ReserveAuthResMsg checkReserveNumber(String reserveNumber){
		String requestId = UUID.randomUUID().toString();

		ReserveAuthReqMsg requestMsg = new ReserveAuthReqMsg(reserveNumber);
		CorrelationData correlationData = new CorrelationData(requestId);
		ParameterizedTypeReference<ReserveAuthResMsg> responseType = new ParameterizedTypeReference<ReserveAuthResMsg>(){};

		ReserveAuthResMsg responseMsg =  rabbitTemplate.convertSendAndReceiveAsType(
			matchRequestQueue,
			requestMsg,
			(MessagePostProcessor) null,
			correlationData,
			responseType);
		String stateCode = responseMsg.getStateCode();

		if(responseMsg == null){
			throw new BusinessException(ResponseCode.MATCH_GENERAL_FAIL);
		}

		if(!stateCode.equals(RabbitMQStateCode.AUTH_SUCCESS.getCode())){
			log.info("[AuthRabbitMQUtil - checkReserveNumber] - stateCode={}, message={}", stateCode, responseMsg.getMessage());
		}

		log.info("[MatchingRabbitMQUtil requestMatch] stateCode = {}", responseMsg.getStateCode());
		return responseMsg;
	}
}
