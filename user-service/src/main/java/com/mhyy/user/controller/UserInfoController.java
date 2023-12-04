package com.mhyy.user.controller;

import com.mhyy.common.response.ResponseVO;
import com.mhyy.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/info")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/check-phone-bind-status")
    public ResponseVO<?> checkPhoneBindStatus(@RequestHeader String personId) {
        return userInfoService.checkPhoneBindStatus(personId);
    }

    @RequestMapping("/bind-phone")
    public ResponseVO<?> bindPhoneNumber(@RequestHeader String personId,
                                         @RequestParam String phoneNumber,
                                         @RequestParam String code) {
        return userInfoService.bindPhoneNumber(personId, phoneNumber, code);
    }

    @RequestMapping("/get-by-token")
    public ResponseVO<?> bindPhoneNumber(@RequestParam String token) {
        return userInfoService.getUserInfoByToken(token);
    }

}
