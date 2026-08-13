package com.starshop.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.wechat")
@Data
public class WeChatProperties {

    private String appid; //小程序的 appid
    private String secret; //小程序的秘钥

}