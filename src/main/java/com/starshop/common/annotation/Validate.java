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
}
