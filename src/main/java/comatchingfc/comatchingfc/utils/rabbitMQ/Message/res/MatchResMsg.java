package comatchingfc.comatchingfc.utils.rabbitMQ.Message.res;

import lombok.Getter;

@Getter
public class MatchResMsg {
	private String stateCode;
	private String enemyUuid;
	private String message;
}
