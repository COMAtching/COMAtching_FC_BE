package comatchingfc.comatchingfc.utils.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {

	//General response
	SUCCESS(200, "GEN-000", HttpStatus.OK, "Success"),
	BAD_REQUEST(400, "GEN-001", HttpStatus.BAD_REQUEST, "Bad Request"),
	INTERNAL_SERVER_ERROR(500, "GEN-002", HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred"),

	//Match response
	MATCH_GENERAL_FAIL(400, "MAT-001", HttpStatus.INTERNAL_SERVER_ERROR, "Matching process was fail"),
	ENEMY_NOT_FOUND(400, "MAT-002", HttpStatus.INTERNAL_SERVER_ERROR, "Can't find enemy"),
	MATCH_ALREADY_EXIST(400, "MAT-003", HttpStatus.INTERNAL_SERVER_ERROR, "Already matched"),
	LACK_OF_MATCH_RESOURCE(400, "MAT-004", HttpStatus.INTERNAL_SERVER_ERROR, "Matching resource is lack"),

	//Notice response
	NO_NOTICE(200, "NOT-001", HttpStatus.OK, "There is any notice exist"),


	//Auth response
	ACCESS_TOKEN_EXPIRED(401, "SEC-001", HttpStatus.UNAUTHORIZED, "AccessToken is expired"),
	REFRESH_TOKEN_EXPIRED(401, "SEC-009", HttpStatus.UNAUTHORIZED, "RefreshToken is expired"),
	JWT_ERROR(403, "SEC-002", HttpStatus.FORBIDDEN, "Jwt error occurred"),
	ACCOUNT_ID_DUPLICATED(409, "SEC-003", HttpStatus.CONFLICT, "AccountId is duplicated"),
	INVALID_LOGIN(403, "SEC-004", HttpStatus.FORBIDDEN, "Invalid AccountId or Password"),
	USER_NOT_FOUND(404, "SEC-005", HttpStatus.NOT_FOUND, "Cannot found user"),
	ACCESS_DENIED(403, "SEC-006", HttpStatus.FORBIDDEN, "Access denied"),
	INVALID_TICKET(403, "SEC-007", HttpStatus.FORBIDDEN, "Invalid ticket"),
	DEACTIVATED_USER(403, "SEC-008", HttpStatus.FORBIDDEN, "Deactivated User");

	private final Integer status;
	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

	ResponseCode(Integer status, String code, HttpStatus httpStatus, String message) {
		this.status = status;
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
	}

}
