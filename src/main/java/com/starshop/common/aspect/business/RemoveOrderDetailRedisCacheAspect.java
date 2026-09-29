package com.starshop.common.aspect.business;

import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.common.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
public class RemoveOrderDetailRedisCacheAspect {
    @Pointcut("@annotation(com.starshop.common.annotation.business.RemoveOrderDetailRedisCacheAnnotation)")
    public void pointCut() {
    }

    @Around("pointCut()")
    @SuppressWarnings("unchecked")
    public Object afterReturnSuccess(ProceedingJoinPoint joinPoint) throws Throwable {
        Object resultObject = joinPoint.proceed();
        if (Objects.isNull(resultObject)) {
            throw new Throwable();
        }
        Result<Object> result = (Result<Object>) resultObject;
        Boolean isSuccess = result.getSuccess();
        if (!isSuccess) {
            return result;
        }
        Object[] args = joinPoint.getArgs();
        if (args.length==0){
            return result;
        }
        Object arg = args[0];
        if (!(arg instanceof String)){
            return result;
        }
        String key = RedisKeyConstant.PREFIX_ORDER + RedisKeyConstant.DETAIL + RedisKeyConstant.ORDER_NO + (String) arg;
        RedisConnector.delete(key);
        return result;
    }
}
