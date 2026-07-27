package com.starshop.interceptor;


import com.starshop.common.utils.JwtUtils;
import com.starshop.constant.JwtClaimsConstant;
import com.starshop.context.BaseContext;
import com.starshop.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 *  jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.判断是动态方法还是其他方法
        if(!(handler instanceof HandlerMethod)){
            //除Controller的动态方法，其他放行
            return true;
        }

        //2.获取请求头的token
        String token = request.getHeader(jwtProperties.getUserTokenName());

        try {
            //3.校验token
            log.info("校验token,{}",token);
            Claims claims = JwtUtils.parseJWT(jwtProperties.getUserSecretKey(), token);
            //4.通过解析的结果拿到user ID
            String userIdStr = claims.get(JwtClaimsConstant.USER_ID).toString();
            Long userId = Long.valueOf(userIdStr);
            log.info("当前用户id是{}",userId);
            //5.通过ThreadLocal在token里面获取当前用户id
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

    }
}
