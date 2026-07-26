package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.annotation.Validate;
import com.starshop.common.creation.NicknameCreation;
import com.starshop.constant.MessageConstant;
import com.starshop.mapper.UserMapper;
import com.starshop.pojo.dto.UserLoginDTO;
import com.starshop.pojo.entity.User;
import com.starshop.result.Result;
import com.starshop.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 新用户注册账号
     * @param userLoginDTO
     * @return
     */
    @Override
    @Transactional
    @Validate
    public Result register(UserLoginDTO userLoginDTO) {
        //1.根据username查询数据库
        User user = lambdaQuery().eq(User::getUsername, userLoginDTO.getUsername()).one();
        //2.判断是否存在
        if(Objects.nonNull(user)){
            //2.1存在，返回错误信息
            return Result.error(MessageConstant.USER_NAME_EXISTS);
        }
        //2.2不存在，构造User
        //2.3加密密码
        String hashpw = BCrypt.hashpw(userLoginDTO.getPassword(), BCrypt.gensalt());
        //2.4生成随机昵称（UUID）
        String nickname = NicknameCreation.createDefaultNickname();
        User build = User.builder()
                .nickname(nickname)
                .password(hashpw)
                .username(userLoginDTO.getUsername())
                .phone(userLoginDTO.getPhone())
                .build();
        //3.写入数据库
        boolean isSuccess = save(build);
        if(!isSuccess){
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        //4.返回结果
        return Result.success();
    }
}
