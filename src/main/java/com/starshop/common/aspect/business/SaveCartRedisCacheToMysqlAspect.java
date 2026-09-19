package com.starshop.common.aspect.business;

import com.starshop.constant.RedisKeyConstant;
import com.starshop.context.BaseContext;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.job.delay.SaveCartRedisCacheDelayJob;
import com.starshop.result.Result;
import jakarta.annotation.Resource;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
public class SaveCartRedisCacheToMysqlAspect {

    @Resource
    private SaveCartRedisCacheDelayJob saveCartRedisCacheDelayJob;


    @Pointcut(value = "@annotation(com.starshop.common.annotation.business.SaveCartRedisCacheToMysqlAnnotation)")
    public void pointCut() {
    }

    /**
     * 清除空对象
     */
    @Before("pointCut()")
    public void beforeMethodExecution(){
        String userId = BaseContext.getUserId();
        String cartKey = RedisKeyConstant.PREFIX_CART + RedisKeyConstant.USER + userId;
        String emptyCartHashKey = RedisKeyConstant.PRODUCT + "0" + "," + RedisKeyConstant.PRODUCT_SPEC + "0";
        RedisConnector.opsForHash().delete(cartKey, emptyCartHashKey);
    }


    /**
     * cart RedisCache 延迟存库
     * @param result
     */
    @AfterReturning(pointcut = "pointCut()", returning = "result")
    public void afterReturnSuccess(Result<?> result) {
        if (Objects.isNull(result)) {
            return;
        }
        Boolean isSuccess = result.getSuccess();
        if (!isSuccess) {
            return;
        }
        String userId = BaseContext.getUserId();
        saveCartRedisCacheDelayJob.setUserIdToDelayedQueue(userId);
    }
}
