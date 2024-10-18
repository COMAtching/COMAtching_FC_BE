package comatchingfc.comatchingfc.utils.rabbitMQ.Message.res;

import comatchingfc.comatchingfc.user.enums.TeamSide;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReserveAuthResMsg {
	private String stateCode;
	private TeamSide teamSide;
	private boolean authSuccess;
	private String message;
}
