package comatchingfc.comatchingfc.user.controller;

import comatchingfc.comatchingfc.admin.entity.Admin;
import comatchingfc.comatchingfc.user.dto.InputFeatureReq;
import comatchingfc.comatchingfc.user.entity.Users;
import comatchingfc.comatchingfc.user.service.UserService;
import comatchingfc.comatchingfc.utils.response.Response;
import comatchingfc.comatchingfc.utils.security.SecurityUtil;
import comatchingfc.comatchingfc.utils.uuid.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

}
