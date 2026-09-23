package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.context.BaseContext;
import com.starshop.mapper.SysUserMapper;
import com.starshop.pojo.entity.SysUser;
import com.starshop.pojo.vo.UserDetailVO;
import com.starshop.result.Result;
import com.starshop.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    @Resource
    private CopyMapper copyMapper;

    /**
     * 获取用户详情
     * @return
     */
    @Override
    public Result getUserDetail() {
        String userId = BaseContext.getUserId();
        SysUser user = lambdaQuery().eq(SysUser::getId, userId).one();
        UserDetailVO userDetailVO = copyMapper.sysUserToUserDetailVO(user);
        return Result.success(userDetailVO);
    }
}
