package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.constant.MessageConstant;
import com.starshop.context.BaseContext;
import com.starshop.mapper.SysUserMapper;
import com.starshop.pojo.dto.UserDetailDTO;
import com.starshop.pojo.entity.SysUser;
import com.starshop.pojo.vo.UserDetailVO;
import com.starshop.result.Result;
import com.starshop.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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

    /**
     * 更改用户详情
     * @param userDetailDTO
     * @return
     */
    @Override
    public Result updateUserDetail(UserDetailDTO userDetailDTO) {
        String userId = BaseContext.getUserId();
        if (StringUtils.isBlank(userDetailDTO.getNickname())) {
            return Result.error(MessageConstant.USER_NAME_NOT_NULL);
        }
        boolean isSuccess = lambdaUpdate().eq(SysUser::getId, userId).set(SysUser::getNickname, userDetailDTO.getNickname())
                .set(SysUser::getAvatar, userDetailDTO.getAvatar()).set(SysUser::getPhone, userDetailDTO.getPhone()).update();
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success();
    }
}
