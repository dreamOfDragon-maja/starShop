package com.starshop.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starshop.properties.WeChatProperties;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WechatLoginUtils {

    @Resource
    private WeChatProperties weChatProperties;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 用code换openid和session_key
     *
     * @param code 前端传的临时 code
     * @return JsonNode 微信返回的结果（包含openid/session_key）
     * @throws RuntimeException 微信接口返回错误码时抛出异常
     */
    public JsonNode getWechatUserInfo(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                weChatProperties.getAppid(),
                weChatProperties.getSecret(),
                code
        );

        // 2. 调用微信GET接口，获取返回的JSON字符串
        String result = restTemplate.getForObject(url, String.class);

        try {
            // 3. 解析JSON字符串为JsonNode对象
            JsonNode jsonNode = objectMapper.readTree(result);

            // 4. 检查微信接口返回的错误码，逻辑和原代码完全一致
            // 微信接口成功返回：无 errcode 字段 或 errcode=0
            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                String errMsg = jsonNode.get("errmsg").asText();
                throw new RuntimeException("微信登录失败：" + errMsg);
            }

            return jsonNode;
        } catch (Exception e) {
            // 解析JSON失败/接口返回非JSON格式时，抛出运行时异常
            throw new RuntimeException("微信登录-解析返回结果失败", e);
        }
    }
}
