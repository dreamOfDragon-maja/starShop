package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.entity.SysUser;
import com.starshop.result.Result;

public interface UserService extends IService<SysUser> {
    /**
     * 获取用户详情
     * @return
     */
    Result getUserDetail();

}
