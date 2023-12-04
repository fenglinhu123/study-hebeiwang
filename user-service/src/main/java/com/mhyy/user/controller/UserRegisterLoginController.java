package com.mhyy.user.controller;

import com.mhyy.common.response.ResponseVO;
import com.mhyy.user.pojo.Oauth2ClientRegister;
import com.mhyy.user.pojo.User;
import com.mhyy.user.service.UserRegisterLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user/register")
public class UserRegisterLoginController {

    @Autowired
    private UserRegisterLoginService userService;

    @PostMapping("/name-password")
    public ResponseVO<?> namePasswordRegister(@RequestBody User user) {
        return userService.namePasswordRegister(user);
    }

    @PostMapping("/phone-code")
    public ResponseVO<?> phoneCodeRegister(@RequestParam String phoneNumber,
                                           @RequestParam String code) {
        return userService.phoneCodeRegister(phoneNumber, code);
    }

    @RequestMapping("/gitee")
    public ResponseVO<?> thirdPartGiteeCallBack(HttpServletRequest request) {
        return userService.thirdPartGitee(request);
    }

    @RequestMapping("/login")
    public ResponseVO<?> login(@RequestParam String username, @RequestParam String password) {
        return userService.login(username, password);
    }

    @RequestMapping("/third-part-app/request")
    public ResponseVO<?> thirdPartAppRequest(@RequestHeader String personId,
                                             @RequestBody Oauth2ClientRegister oauth2ClientRegister) {
        return userService.thirdPartAppRequest(personId, oauth2ClientRegister);
    }

    @RequestMapping("/third-part-app/request/status")
    public ResponseVO<?> checkThirdAppRequestStatus(@RequestHeader String personId) {
        return userService.checkThirdParAppRequestStatus(personId);
    }

    @RequestMapping("/third-part-app/request/approve")
    public ResponseVO<?> checkThirdAppRequestApprove(@RequestParam String appName) {
        return userService.checkThirdParAppRequestApprove(appName);
    }
}
