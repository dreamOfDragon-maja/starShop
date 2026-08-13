package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.common.result.UserInfo;
import com.starshop.pojo.dto.UserLoginDTO;
import com.starshop.pojo.dto.UserUpdateDTO;
import com.starshop.pojo.dto.UserWechatDTO;
import com.starshop.pojo.entity.SysUser;
import com.starshop.result.Result;
import jakarta.validation.constraints.NotBlank;

public interface UserService extends IService<SysUser> {

    /**
     * 根据 username 查询 user 带角色和权限
     */
    SysUser getSysUserByNameWithRolesAndPermissions(String username);
    /**
     * 根据 userId 查询 user 带角色和权限
     */
    SysUser getSysUserByUserIdWithRolesAndPermissions(Long userId);
    /**
     * 根据 openid 查询 user 带角色和权限
     */
    SysUser getSysUserByOpenidWithRolesAndPermissions(String openid);


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
    Result<Object> getUser();

    /**
     * 更新用户信息
     * @param userUpdateDTO
     * @return
     */
    Result<UserInfo> updateUserInfo(UserUpdateDTO userUpdateDTO);

    /**
     * 修改密码
     * @param username
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    Result changePassword(String username, String passwordOld, String passwordNew);


    /**
     * 忘记密码
     * @param username
     * @param phone
     * @param passwordNew
     * @return
     */
    Result forgetPassword(String username, String phone, String passwordNew);

    /**
     * 用户使用微信快速登录
     * @return
     */
    Result loginByWechat(UserWechatDTO userWechatDTO);

}
