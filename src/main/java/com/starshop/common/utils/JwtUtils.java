package com.starshop.common.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtils {

    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt 秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        //生成JWT时间
        long l = System.currentTimeMillis() + ttlMillis;
        Date date = new Date(l);
        //构造结果返回
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
                .setClaims(claims)
                .setExpiration(date)
                .compact();
    }


    /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
         return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                 .build()
                 // 设置需要解析的jwt
                .parseClaimsJws(token)
                .getBody();
    }
}
