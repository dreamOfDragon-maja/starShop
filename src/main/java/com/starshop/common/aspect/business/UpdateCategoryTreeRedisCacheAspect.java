package com.starshop.common.aspect.business;

import com.starshop.result.Result;
import com.starshop.service.CategoryService;
import jakarta.annotation.Resource;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
public class UpdateCategoryTreeRedisCacheAspect {

      @Pointcut("@annotation(com.starshop.common.annotation.UpdateCategoryTreeRedisCacheAnnotation)")
      private void pointCut(){
      }

      @Resource
      private CategoryService categoryService;

      @AfterReturning(pointcut = "pointCut()", returning = "result")
      public void afterReturnSuccess(Result<Object> result) {
          if (Objects.isNull(result)) {
              return;

          }
          boolean isSuccess = result.getSuccess();
          if (!isSuccess) {
              return;
          }
          categoryService.updateCategoryTreeRedisCache();

      }
}
