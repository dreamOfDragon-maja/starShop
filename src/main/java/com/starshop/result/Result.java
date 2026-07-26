package com.starshop.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果
 */
@Data
public class Result<T> implements Serializable {

    //1说明成功，0和其他数字说明失败
    private Integer code;
    //错误信息
    private String msg;
    //返回数据
    private T data;

    //返回成功（无参）
    public static <T> Result<T> success(){
        Result<T> result = new Result<T>();
        result.code =1;
        return result;
    }

    //返回成功（有参）
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<T>();
        result.code =1;
        result.data = data;
        return result;
    }

    //返回失败
    public static <T> Result<T> error(String msg){
        Result<T> result = new Result<T>();
        result.code =0;
        result.msg = msg;
        return result;
    }
}
