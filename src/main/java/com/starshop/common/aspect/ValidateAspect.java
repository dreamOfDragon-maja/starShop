package com.starshop.common.aspect;

import com.starshop.common.annotation.Validate;
import com.starshop.constant.MessageConstant;
import com.starshop.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 创建校验切面
 */
@Aspect  //表明切面类
@Component
public class ValidateAspect {
    /**
     * 定义切点，匹配注解
     */
    @Pointcut("@annotation(com.starshop.common.annotation.Validate)")
    public void validatePointcut() {
    }

    @Around("validatePointcut()")
    public Object doValidate(ProceedingJoinPoint point) throws Throwable {
        //1.获取注解上的requiredPhone配置
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        boolean requiredPhone = method.getAnnotation(Validate.class).requiredPhone();

        //2.遍历参数
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (isDto(arg)) {
                //3.执行逻辑
                Result result = validateDto(arg,requiredPhone);
                //4.判断是否成功
                if (result != null) {
                    return result;
                }
            }
        }
        // 5. 校验通过，执行原方法
        return point.proceed();
    }

    /**
     * 具体校验逻辑
     *
     * @param dto
     * @return
     */
    private Result validateDto(Object dto, boolean requiredPhone) {
        Class<?> clazz = dto.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            if (!String.class.equals(field.getType())) continue;

            String value = getStringValue(dto, field);

            switch (fieldName) {
                case "username":
                    if (StringUtils.isBlank(value)) return Result.error(MessageConstant.USERNAME_NOT_EMPTY);
                    if (value.length() < 3 || value.length() > 20) return Result.error(MessageConstant.USERNAME_LENGTH_ERROR);
                    if (!value.matches("^[a-zA-Z0-9_]+$")) return Result.error(MessageConstant.USERNAME_PATTERN_ERROR);
                    break;
                case "password":
                    if (StringUtils.isBlank(value)) return Result.error(MessageConstant.PASSWORD_NOT_EMPTY);
                    if (value.length() < 6 || value.length() > 20) return Result.error(MessageConstant.PASSWORD_LENGTH_ERROR);
                    break;
                case "phone":
                    if (StringUtils.isBlank(value)) {  // 用 value 代替 phone
                        if (requiredPhone) return Result.error(MessageConstant.PHONE_NOT_EMPTY);
                    } else if (!value.matches("^1[3-9]\\d{9}$")) {  // 用 value 代替 phone
                        return Result.error(MessageConstant.PHONE_PATTERN_ERROR);
                    }
                    break;
            }
        }
        return null;
    }


    /**
     * 判断是否为 DTO 类
     */
    private boolean isDto(Object obj) {
        return obj != null && obj.getClass().getName().contains(".pojo.dto.");
    }

    /**
     * 获取字段值
     */
    private String getStringValue(Object obj, Field field) {
        try {
            String getter = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            Method m = obj.getClass().getMethod(getter);
            Object v = m.invoke(obj);
            return v == null ? null : v.toString();
        } catch (Exception e) {
            try {
                field.setAccessible(true);
                Object v = field.get(obj);
                return v == null ? null : v.toString();
            } catch (IllegalAccessException ex) {
                return null;
            }
        }
    }
}
