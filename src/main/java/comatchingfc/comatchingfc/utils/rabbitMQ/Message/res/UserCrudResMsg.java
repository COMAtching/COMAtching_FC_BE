package comatchingfc.comatchingfc.utils.rabbitMQ.Message.res;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Getter
public class UserCrudResMsg {

	private String stateCode;
	private String enemyUuid;
	private String message;

	public String toJsonString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
