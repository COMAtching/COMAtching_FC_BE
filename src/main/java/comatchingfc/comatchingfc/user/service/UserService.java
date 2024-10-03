package comatchingfc.comatchingfc.user.service;

import comatchingfc.comatchingfc.user.dto.InputFeatureReq;
import comatchingfc.comatchingfc.user.repository.UserRepository;
import comatchingfc.comatchingfc.utils.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public void inputFeature(InputFeatureReq inputFeatureReq) {

    }


}
