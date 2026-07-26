package com.starshop.common.aspect;

import com.starshop.constant.MessageConstant;
import com.starshop.pojo.dto.UserLoginDTO;
import com.starshop.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

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
        //1.获取方法参数
        Object[] args = point.getArgs();
        //2.遍历args
        for (Object arg : args) {
            if (arg instanceof UserLoginDTO) {
                UserLoginDTO userLoginDTO = (UserLoginDTO) arg;
                //3.执行逻辑
                Result result = validateUserLoginDTO(userLoginDTO);
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
    private Result validateUserLoginDTO(UserLoginDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();
        String phone = dto.getPhone();

        // 用户名校验
        if (StringUtils.isBlank(username)) return Result.error(MessageConstant.USERNAME_NOT_EMPTY);
        if (username.length() < 3 || username.length() > 20) return Result.error(MessageConstant.USERNAME_LENGTH_ERROR);
        if (!username.matches("^[a-zA-Z0-9_]+$")) return Result.error(MessageConstant.USERNAME_PATTERN_ERROR);

        // 密码校验
        if (StringUtils.isBlank(password)) return Result.error(MessageConstant.PASSWORD_NOT_EMPTY);
        if (password.length() < 6 || password.length() > 20) return Result.error(MessageConstant.PASSWORD_LENGTH_ERROR);

        // 手机号校验
        if (StringUtils.isBlank(phone)) return Result.error(MessageConstant.PHONE_NOT_EMPTY);
        if (!phone.matches("^1[3-9]\\d{9}$")) return Result.error(MessageConstant.PHONE_PATTERN_ERROR);

        return null;
    }
}