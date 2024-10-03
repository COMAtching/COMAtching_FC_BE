package comatchingfc.comatchingfc.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputFeatureReq {
    private String username;
    private String gender;
    private int age;
    private String socialId;
    private String cheeringPlayer;
}
