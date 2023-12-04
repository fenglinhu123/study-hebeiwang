package com.mhyy.user.service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.mhyy.common.response.ResponseCode;
import com.mhyy.common.response.ResponseVO;
import com.mhyy.user.pojo.User;
import com.mhyy.user.processor.RedisCommonProcessor;
import com.mhyy.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserInfoService {

    @Autowired
    private RedisCommonProcessor redisCommonProcessor;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate innerRestTemplate;

    public ResponseVO<?> checkPhoneBindStatus(String personId) {
        User user = (User) redisCommonProcessor.get(personId);
        boolean isBind = false;
        if (user != null) {
            isBind = user.getUserPhone() != null;
            return ResponseVO.success(isBind);
        }
        Integer userId = Integer.parseInt(personId) - 10000000;
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            isBind = userOptional.get().getUserPhone() != null;
            redisCommonProcessor.setExpiredDays(personId, userOptional.get(), 30);
            return ResponseVO.success(isBind);
        }
        return ResponseVO.fail(ResponseCode.BAD_REQUEST.getReturnCode(), "Invalid user");
    }

    public ResponseVO<?> bindPhoneNumber(String personId, String phoneNumber, String code) {
        String cacheCode = redisCommonProcessor.get(phoneNumber) == null ? null : String.valueOf(redisCommonProcessor.get(phoneNumber));
        if (StringUtils.isEmpty(cacheCode)) {
            return ResponseVO.fail(ResponseCode.BAD_REQUEST.getReturnCode(), "Phone code is Expired!");
        }

        if (!cacheCode.equals(code)) {
            return ResponseVO.fail(ResponseCode.BAD_REQUEST.getReturnCode(), "Phone code is wrong!");
        }
        Integer userId = Integer.parseInt(personId) - 10000000;
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return ResponseVO.fail(ResponseCode.BAD_REQUEST.getReturnCode(), "Invalid user!");
        }
        userRepository.updateUserPhoneById(phoneNumber, userId);
        redisCommonProcessor.remove(personId);
        return ResponseVO.success();
    }

    public ResponseVO<?> getUserInfoByToken(String token) {
        Map results =
                innerRestTemplate.getForObject("http://oauth2-service/oauth/check_token?token=" + token, Map.class);
        System.out.println(JSONObject.toJSONString(results));
        assert results != null;
        boolean active = Boolean.parseBoolean(String.valueOf(results.get("active")));
        System.out.println(String.valueOf(results.get("active"))+Boolean.getBoolean(String.valueOf(results.get("active"))));
        if (!active) {
            return ResponseVO.fail(ResponseCode.UNAUTHORIZED.getReturnCode(), "token is not active");
        }
        String username = String.valueOf(results.get("user_name"));
        return ResponseVO.success(new HashMap() {{
            put("username", username);
        }});
    }
}
