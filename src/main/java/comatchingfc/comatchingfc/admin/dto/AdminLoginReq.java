package comatchingfc.comatchingfc.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginReq {
    private String accountId;
    private String password;
}
