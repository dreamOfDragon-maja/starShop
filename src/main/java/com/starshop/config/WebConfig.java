package com.starshop.config;

import com.starshop.interceptor.JwtInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器并指定拦截路径
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
            log.info("开始自定义注册拦截器");
            registry.addInterceptor(jwtInterceptor)
                    .addPathPatterns("/api/**")
              //排除登录和注册接口
                    .excludePathPatterns("/api/user/login",
                                        "/api/user/register",
                                        "/api/v1/test/**");

    }
}
