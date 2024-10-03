package comatchingfc.comatchingfc.auth.oauth2.service;

import comatchingfc.comatchingfc.auth.oauth2.dto.CustomOAuth2User;
import comatchingfc.comatchingfc.auth.oauth2.dto.KakaoResponse;
import comatchingfc.comatchingfc.auth.oauth2.dto.OAuth2Response;
import comatchingfc.comatchingfc.auth.oauth2.dto.UserDto;
import comatchingfc.comatchingfc.user.entity.UserAiInfo;
import comatchingfc.comatchingfc.user.entity.Users;
import comatchingfc.comatchingfc.user.repository.UserAiInfoRepository;
import comatchingfc.comatchingfc.user.repository.UserRepository;
import comatchingfc.comatchingfc.utils.uuid.UUIDUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserAiInfoRepository userAiInfoRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        OAuth2Response oAuth2Response = null;

        if(userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String identifyKey = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        Optional<Users> userOpt = userRepository.findByIdentifyKey(identifyKey);
        UserDto userDto;

        if (userOpt.isEmpty()) {
            Users newUser = Users.builder()
                    .identifyKey(identifyKey)
                    .build();

            byte[] uuid = UUIDUtil.createUUID();
            UserAiInfo newUserAiInfo = UserAiInfo.builder()
                    .uuid(uuid)
                    .build();
            newUserAiInfo.setUsers(newUser);

            userRepository.save(newUser);
            userAiInfoRepository.save(newUserAiInfo);

            userDto = UserDto.builder()
                .uuid(UUIDUtil.bytesToHex(uuid))
                .role(newUser.getRole().toString())
                .build();
        } else {
            Users existUser = userOpt.get();
            byte[] uuid = existUser.getUserAiInfo().getUuid();

            userDto = UserDto.builder()
                    .uuid(UUIDUtil.bytesToHex(uuid))
                    .role(existUser.getRole().toString())
                    .build();
        }

        return new CustomOAuth2User(userDto);
    }
}
