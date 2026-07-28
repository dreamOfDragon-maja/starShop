package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.UserLoginDTO;
import com.starshop.pojo.entity.User;
import com.starshop.pojo.vo.UserVO;
import com.starshop.result.Result;
import jakarta.validation.constraints.NotBlank;

public interface UserService extends IService<User> {
    /**
     * 新用户注册账号
     * @param userLoginDTO
     * @return
     */
    Result register(UserLoginDTO userLoginDTO);

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    Result<Object> login(UserLoginDTO userLoginDTO) throws Exception;

    /**
     * 刷新token
     * @param refreshToken
     * @return
     */
    Result refreshToken(@NotBlank String refreshToken);

    /**
     * 获取当前用户信息
     * @return
     */
    Result<UserVO> getUser();

}
