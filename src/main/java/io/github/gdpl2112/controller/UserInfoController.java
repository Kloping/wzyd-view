package io.github.gdpl2112.controller;

import io.github.gdpl2112.service.UserInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User information HTTP adapter. Profile aggregation and rendering lives in
 * {@link UserInfoService}.
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {
    private final UserInfoService userInfoService;

    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @RequestMapping("/")
    public ResponseEntity<String> getUserInfo(
            @RequestParam(name = "sid", required = false, defaultValue = "") String sid,
            @RequestParam(name = "uid", required = false, defaultValue = "") String uid,
            HttpServletResponse response) {
        return userInfoService.getUserInfo(sid, uid, response);
    }
}
