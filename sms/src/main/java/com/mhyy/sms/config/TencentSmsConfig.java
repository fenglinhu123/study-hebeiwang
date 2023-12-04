package com.mhyy.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "tencent.sms")
public class TencentSmsConfig {
    private String secretId;
    private String secretKey;
    private String region;
    private String appId;
    private String signName;
    private TencentTemplateConfig templateId;
}
