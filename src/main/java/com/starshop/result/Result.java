package com.starshop.result;

import com.starshop.common.result.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.Objects;

/**
 * 统一返回结果
 */
@Data
public class Result<T> implements Serializable {

    //请求是否成功（true=成功，false=失败）
    private Boolean success;
    //1说明成功，0和其他数字说明失败
    private Integer code;
    //错误信息
    private String msg;
    //返回数据
    private T data;

    //返回成功（无参）
    public static <T> Result<T> success(){
        Result<T> result = new Result<T>();
        result.success = true;
        result.code =1;
        return result;
    }

    //返回成功（有参）
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<T>();
        result.success = true;
        result.code =1;
        result.data = data;
        return result;
    }

    //返回失败
    public static <T> Result<T> error(String msg){
        Result<T> result = new Result<T>();
        result.success = false;
        result.code =0;
        result.msg = msg;
        return result;
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        Result result = new Result();
        result.success = false;
        result.code = resultCode.getCode();
        result.msg = resultCode.getMessage();
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result result = new Result();
        result.success = false;
        result.code = code;
        result.msg = message;
        return result;
    }

    public static <T> Result<T> error(int httpStatusCode, String message) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = null;
        if (Objects.nonNull(attributes)) {
            response = attributes.getResponse();
        }

        if (Objects.nonNull(response)) {
            response.setStatus(httpStatusCode);
        }

        return error((Integer) httpStatusCode, message);
    }

    public static <T> Result<T> error(int httpStatusCode, Integer code, String message) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = null;
        if (Objects.nonNull(attributes)) {
            response = attributes.getResponse();
        }

        if (Objects.nonNull(response)) {
            response.setStatus(httpStatusCode);
        }

        return error(code, message);
    }
}
