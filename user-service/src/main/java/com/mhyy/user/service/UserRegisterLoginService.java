package com.mhyy.user.service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mhyy.common.response.ResponseCode;
import com.mhyy.common.response.ResponseVO;
import com.mhyy.user.config.GiteeConfig;
import com.mhyy.user.pojo.*;
import com.mhyy.user.processor.RedisCommonProcessor;
import com.mhyy.user.repo.OauthClientRegisterRepository;
import com.mhyy.user.repo.OauthClientRepository;
import com.mhyy.user.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class UserRegisterLoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OauthClientRepository oauthClientRepository;

    @Autowired
    private RedisCommonProcessor redisCommonProcessor;

    @Autowired
    private RestTemplate innerRestTemplate;

    @Autowired
    private RestTemplate outerRestTemplate;

    @Autowired
    private GiteeConfig giteeConfig;

    @Autowired
    private OauthClientRegisterRepository oauthClientRegisterRepository;

    @Resource(name = "transactionManager")
    private PlatformTransactionManager transactionManager;

    public ResponseVO<?> namePasswordRegister(User user) {
        if (userRepository.findByUserName(user.getUserName()) == null
                && oauthClientRepository.findByClientId(user.getUserName()) == null) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String password = user.getPasswd();
            String encodePassword = bCryptPasswordEncoder.encode(password);
            user.setPasswd(encodePassword);

            Oauth2Client oauth2Client = Oauth2Client.builder()
                    .clientId(user.getUserName())
                    .clientSecret(encodePassword)
                    .resourceIds(RegisterType.USER_PASSWORD.name())
                    .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",").concat(AuthGrantType.password.name()))
                    .scope("web")
                    .authorities(RegisterType.USER_PASSWORD.name())
                    .build();
            Integer uid = this.saveUserAndOAuthClient(user, oauth2Client);
            String personId = uid + 10000000 + "";
            redisCommonProcessor.setExpiredDays(personId, user, 30);
            return ResponseVO.success(
                    formatResponseContent(user,
                            generateOauthToken(AuthGrantType.password, user.getUserName(), password, user.getUserName(), password))
            );

        }
        return ResponseVO.fail(ResponseCode.BAD_REQUEST.getReturnCode(), "User already exist! Please login!");
    }

    public ResponseVO<?> phoneCodeRegister(String phoneNumber, String code) {
        String cacheCode = redisCommonProcessor.get(phoneNumber) == null ? null : String.valueOf(redisCommonProcessor.get(phoneNumber));
        if (StringUtils.isEmpty(cacheCode)) {
            return ResponseVO.fail(ResponseCode.BAD_REQUEST.getReturnCode(), "Phone code is Expired!");
        }

        if (!cacheCode.equals(code)) {
            return ResponseVO.fail(ResponseCode.BAD_REQUEST.getReturnCode(), "Phone code is wrong!");
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodePasswd = bCryptPasswordEncoder.encode(code);
        User user = userRepository.findByUserPhone(phoneNumber);
        if (user == null) {
            String username = getSystemDefinedUserName(phoneNumber);
            user = User.builder()
                    .userName(username)
                    .passwd("")
                    .userPhone(phoneNumber)
                    .userRole(RegisterType.PHONE_NUMBER.name())
                    .build();
            Oauth2Client oauth2Client = Oauth2Client.builder()
                    .clientId(phoneNumber)
                    .clientSecret(encodePasswd)
                    .resourceIds(RegisterType.PHONE_NUMBER.name())
                    .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",")
                            .concat(AuthGrantType.client_credentials.name()))
                    .scope("web")
                    .authorities(RegisterType.PHONE_NUMBER.name())
                    .build();
            Integer uid = this.saveUserAndOAuthClient(user, oauth2Client);
            String personId = uid + 10000000 + "";
            redisCommonProcessor.setExpiredDays(personId, user, 30);
        } else {
            oauthClientRepository.updateSecretByClientId(encodePasswd, phoneNumber);
        }
        return ResponseVO.success(formatResponseContent(user,
                generateOauthToken(AuthGrantType.client_credentials, null, null, phoneNumber, code)));
    }

    public ResponseVO<?> thirdPartGitee(HttpServletRequest request) {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        if (!giteeConfig.getState().equals(state)) {
            throw new UnsupportedOperationException("Invalid state!");
        }
        String tokenUrl = String.format(giteeConfig.getTokenUrl(),
                giteeConfig.getClientId(), giteeConfig.getClientSecret(), giteeConfig.getCallBack(), code);
        JSONObject tokenResult = outerRestTemplate.postForObject(tokenUrl, null, JSONObject.class);
        log.info("返回的参数: {} ", JSON.toJSONString(tokenResult));
        String token = String.valueOf(tokenResult.get("access_token"));

        String userUrl = String.format(giteeConfig.getUserUrl(), token);
        JSONObject userInfo = outerRestTemplate.getForObject(userUrl, JSONObject.class);
        log.info("返回的用户信息: {}", JSON.toJSONString(userInfo));

        String username = giteeConfig.getState().concat("_").concat(String.valueOf(userInfo.get("name")));
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodePassword = bCryptPasswordEncoder.encode(username);
        User user = userRepository.findByUserName(username);
        if (user == null) {
            user = User.builder()
                    .userName(username)
                    .passwd(encodePassword)
                    .userRole(RegisterType.THIRD_PARTY.name())
                    .build();
            Oauth2Client oauth2Client = Oauth2Client.builder()
                    .clientId(username)
                    .clientSecret(encodePassword)
                    .resourceIds(RegisterType.THIRD_PARTY.name())
                    .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",")
                            .concat(AuthGrantType.client_credentials.name()))
                    .scope("web")
                    .authorities(RegisterType.THIRD_PARTY.name())
                    .build();
            Integer uid = this.saveUserAndOAuthClient(user, oauth2Client);
            String personId = uid + 10000000 + "";
            redisCommonProcessor.setExpiredDays(personId, user, 30);
        }
        return ResponseVO.success(formatResponseContent(user,
                generateOauthToken(AuthGrantType.client_credentials, null, null, username, username)));
    }

    private String getSystemDefinedUserName(String phoneNumber) {
        return "MALL_" + System.currentTimeMillis() + phoneNumber.substring(phoneNumber.length() - 4);
    }

    private Map formatResponseContent(User user, Map oauth2Client) {
        return new HashMap<String, Object>() {{
            put("user", user);
            put("oauth", oauth2Client);
        }};
    }

    private Integer saveUserAndOAuthClient(User user, Oauth2Client oauth2Client) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setTimeout(30);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            user = this.userRepository.save(user);
            this.oauthClientRepository.save(oauth2Client);
            transactionManager.commit(status);
        } catch (Exception e) {
            if (status.isCompleted()) {
                transactionManager.rollback(status);
            }
            throw new UnsupportedOperationException("DB Save failed!");
        }
        return user.getId();
    }

    private Map generateOauthToken(AuthGrantType authGrantType, String username, String password,
                                   String clientId, String clientSecret) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", authGrantType.name());
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        if (authGrantType == AuthGrantType.password) {
            params.add("username", username);
            params.add("password", password);
        }
        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(params, httpHeaders);
        return innerRestTemplate.postForObject("http://oauth2-service/oauth/token", requestEntity, Map.class);
    }

    public ResponseVO<?> login(String username, String password) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            return ResponseVO.fail(ResponseCode.BAD_REQUEST.getReturnCode(), "user not exist!");
        }

        Map content = formatResponseContent(user,
                generateOauthToken(AuthGrantType.password, username, password, username, password));
        String personId = user.getId() + 10000000 + "";
        redisCommonProcessor.setExpiredDays(personId, user, 30);
        return ResponseVO.success(content);
    }

    public ResponseVO<?> thirdPartAppRequest(String personId, Oauth2ClientRegister oauth2ClientRegister) {
        oauth2ClientRegister.setClientId(UUID.randomUUID().toString().replaceAll("-", ""));
        oauth2ClientRegister.setClientSecret(UUID.randomUUID().toString().replaceAll("-", ""));
        oauth2ClientRegister.setApprove(0);
        oauth2ClientRegister.setUserId(Integer.parseInt(personId) - 10000000);
        oauthClientRegisterRepository.save(oauth2ClientRegister);
        return ResponseVO.success();
    }

    public ResponseVO<?> checkThirdParAppRequestStatus(String personId) {
        Integer userId = Integer.parseInt(personId) - 10000000;
        List<Oauth2ClientRegister> userRequestInfo = oauthClientRegisterRepository.findByUserId(userId);
        return ResponseVO.success(userRequestInfo);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseVO<?> checkThirdParAppRequestApprove(String appName) {
        Oauth2ClientRegister oauth2ClientRegister = this.oauthClientRegisterRepository.findByAppName(appName);
        this.oauthClientRegisterRepository.updateRegisterClientByAppName(appName);
        Oauth2Client oauth2Client = Oauth2Client.builder()
                .clientId(oauth2ClientRegister.getClientId())
                .clientSecret(new BCryptPasswordEncoder().encode(oauth2ClientRegister.getClientSecret()))
                .resourceIds(appName)
                .authorities(appName)
                .autoApprove("true")
                .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",")
                        .concat(AuthGrantType.authorization_code.name()))
                .build();
        this.oauthClientRepository.save(oauth2Client);
        return ResponseVO.success();
    }
}
