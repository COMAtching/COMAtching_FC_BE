package comatchingfc.comatchingfc.auth.dto;

import lombok.Getter;

@Getter
public class UserLoginReq {
    private String type;
    private String ticket;
}
