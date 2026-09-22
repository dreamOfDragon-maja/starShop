package com.starshop.common.aspect.business;

import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.result.Result;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
public class RemoveBannerRedisCacheAspect {

    @Pointcut(value = "@annotation(com.starshop.common.annotation.business.RemoveBannerRedisCacheAnnotation)")
    public void pointcut(){}

    @AfterReturning(pointcut = "pointcut()" ,returning = "result")
    public void afterReturnSuccess(Result<?> result){
        if (Objects.isNull(result)){
            return;
        }
        if (!result.getSuccess()) {
            return;
        }
        RedisConnector.delete(RedisKeyConstant.PREFIX_BANNER + RedisKeyConstant.ALL);

    }
}
