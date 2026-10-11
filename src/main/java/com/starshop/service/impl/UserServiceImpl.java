package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.result.UserInfo;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.context.BaseContext;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.mapper.SysUserMapper;
import com.starshop.pojo.dto.UserDetailDTO;
import com.starshop.pojo.dto.UserUpdateDTO;
import com.starshop.pojo.entity.SysUser;
import com.starshop.pojo.vo.UserDetailVO;
import com.starshop.common.result.Result;
import com.starshop.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

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
     * @param userUpdateDTO
     * @return
     */
    @Override
    @Transactional
    public Result updateUserDetail(UserUpdateDTO userUpdateDTO) {
        //1.查询当前用户
        Long userId = Long.valueOf(BaseContext.getUserId());
        //2.判断用户在数据库中是否存在
        SysUser sysUser = lambdaQuery().eq(SysUser::getId, userId).one();
        if (Objects.isNull(sysUser)) {
            return Result.error(MessageConstant.USER_NOT_EXISTS);
        }
        //3.利用mapstruct更新数据
        copyMapper.updateUserFromDTO(userUpdateDTO, sysUser);
        //4.数据同步到数据库
        boolean isSuccess = updateById(sysUser);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_UPDATE_ERROR);
        }
        //5.封装VO
        UserInfo userInfo = copyMapper.usertoUserInfo(sysUser);
        //6.将数据同步到redis
        String key = RedisKeyConstant.PREFIX_LOGIN + RedisKeyConstant.USER + userId;
        if (RedisConnector.hasKey(key)) {
            RedisConnector.opsForHash().put(key, SysUser.Fields.userInfo, userInfo);
        }

        return Result.success(userInfo);
    }
}
