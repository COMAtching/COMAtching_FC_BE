package comatchingfc.comatchingfc.utils.response;

import lombok.Getter;

@Getter
public enum RabbitMQStateCode {

	//MATCHING queue
	MATCH_SUCCESS("MTCH-000", "MATCHING"),
	MATCH_FIELD_MISSING("MTCH-001", "MATCHING"),
	INVALID_TEAM_OPTION("MTCH-002", "MATCHING"),
	CSV_FILE_IS_EMPTY("MTCH-003", "MATCHING"),
	FILE_OPEN_FAIL("MTCH-004", "MATCHING"),
	ERROR_RUNNING_MODEL("MTCH-004", "MATCHING"),
	MODEL_RETURN_ERROR("MTCH-004", "MATCHING"),

	//USER CRUD queue
	CRUD_SUCCESS("MTCH-000", "MATCHING"),
	CRUD_FIELD_MISSING("MTCH-001", "MATCHING"),
	DECODE_ERROR("MTCH-002", "MATCHING"),
	HEADER_NOT_EQUAL_QUEUE("MTCH-003", "MATCHING"),
	CHECK_USER_IN_CSV("MTCH-004", "MATCHING"),

	//AUTHENTICATION queue
	AUTH_SUCCESS("MTCH-004", "MATCHING"),
	INVAILD_RESERVE("MTCH-004", "MATCHING");

	private String code;
	private String queue;

	RabbitMQStateCode(String code, String queue){
		this.code = code;
		this.queue = queue;
	}

}
