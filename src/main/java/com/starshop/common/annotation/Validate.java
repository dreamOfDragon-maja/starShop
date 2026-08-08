package com.starshop.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 *  创建校验注解
 */
@Target(ElementType.METHOD) //只能加在方法上
@Retention(RetentionPolicy.RUNTIME) //运行时保留
public @interface Validate {

    /**
     * 手机号是否必填
     * - true: 手机号为空时报错
     * - false: 手机号为空时跳过校验
     */
    boolean requiredPhone() default false;
}
