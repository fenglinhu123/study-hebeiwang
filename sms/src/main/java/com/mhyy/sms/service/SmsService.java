package com.mhyy.sms.service;

import com.mhyy.sms.config.TencentSmsConfig;
import com.mhyy.sms.processor.RedisCommonProcessor;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.DescribePhoneNumberInfoRequest;
import com.tencentcloudapi.sms.v20210111.models.DescribePhoneNumberInfoResponse;
import com.tencentcloudapi.sms.v20210111.models.PhoneNumberInfo;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    @Autowired
    private TencentSmsConfig tencentSmsConfig;

    @Autowired
    private RedisCommonProcessor processor;

    public void sendSms(String phoneNumber) {
        try {
            Credential credential = new Credential(tencentSmsConfig.getSecretId(), tencentSmsConfig.getSecretKey());

            // 选择性配置内容
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setConnTimeout(60);
            httpProfile.setReqMethod(HttpProfile.REQ_POST);

            // 可选性配置内容
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            SmsClient client = new SmsClient(credential, tencentSmsConfig.getRegion(), clientProfile);
            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(tencentSmsConfig.getAppId());
            req.setSignName(tencentSmsConfig.getSignName());
            req.setTemplateId(tencentSmsConfig.getTemplateId().getPhoneCode());

            String code = getRandomPhoneCode();
            String[] templateParamSet = {code};
            req.setTemplateParamSet(templateParamSet);

            String[] phoneNumberSet = {phoneNumber};
            req.setPhoneNumberSet(phoneNumberSet);

            client.SendSms(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map getNationalCode(SmsClient client, String[] phoneNumberSet) {
        DescribePhoneNumberInfoRequest request = new DescribePhoneNumberInfoRequest();
        request.setPhoneNumberSet(phoneNumberSet);
        HashMap<String, String> mapResults = new HashMap<>();
        try {
            DescribePhoneNumberInfoResponse response = new DescribePhoneNumberInfoResponse();
            PhoneNumberInfo[] phoneNumberInfoSet = response.getPhoneNumberInfoSet();
            for (int i = 0; i < phoneNumberInfoSet.length; i++) {
                mapResults.put(phoneNumberInfoSet[0].getPhoneNumber(),phoneNumberInfoSet[0].getNationCode());
            }
            return mapResults;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String getRandomPhoneCode() {
        return String.valueOf((Math.random() * 9 + 1) * 100000);
    }
}
