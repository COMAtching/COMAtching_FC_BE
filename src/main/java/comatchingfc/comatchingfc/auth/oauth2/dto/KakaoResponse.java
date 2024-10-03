package comatchingfc.comatchingfc.auth.oauth2.dto;

import lombok.Data;

import java.util.Map;

@Data
public class KakaoResponse implements OAuth2Response{

    private final Map<String, Object> attributes;
    private Map<String, Object> kakaoAccountAttributes;

    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccountAttributes = (Map<String, Object>)attributes.get("kakao_account");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }
}
