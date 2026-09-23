package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.UserDetailDTO;
import com.starshop.pojo.entity.SysUser;
import com.starshop.result.Result;
import jakarta.validation.constraints.NotNull;

public interface UserService extends IService<SysUser> {
    /**
     * 获取用户详情
     * @return
     */
    Result getUserDetail();

    /**
     * 更改用户详情
     * @param userDetailDTO
     * @return
     */
    Result updateUserDetail(@NotNull UserDetailDTO userDetailDTO);
}
