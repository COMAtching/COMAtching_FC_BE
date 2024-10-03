package comatchingfc.comatchingfc.utils.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {

	//General response
	SUCCESS(200, "GEN-000", HttpStatus.OK, "Success"),
	BAD_REQUEST(400, "GEN-001", HttpStatus.BAD_REQUEST, "Bad Request"),
	INTERNAL_SERVER_ERROR(500, "GEN-002", HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred"),

	//Auth response
	TOKEN_EXPIRED(401, "SEC-001", HttpStatus.UNAUTHORIZED, "Token is expired"),
	JWT_ERROR(403, "SEC-002", HttpStatus.FORBIDDEN, "Jwt error occurred"),
	ACCOUNT_ID_DUPLICATED(409, "SEC-003", HttpStatus.CONFLICT, "AccountId is duplicated"),
	INVALID_LOGIN(403, "SEC-004", HttpStatus.FORBIDDEN, "Invalid AccountId or Password"),
	USER_NOT_FOUND(404, "SEC-005", HttpStatus.NOT_FOUND, "Cannot found user"),
	ACCESS_DENIED(403, "SEC-004", HttpStatus.FORBIDDEN, "Access denied");

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
