package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.annotation.Validate;
import com.starshop.common.creation.NicknameCreation;
import com.starshop.common.utils.JwtUtils;
import com.starshop.constant.JwtClaimsConstant;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.mapper.UserMapper;
import com.starshop.pojo.dto.UserLoginDTO;
import com.starshop.pojo.entity.User;
import com.starshop.pojo.vo.UserLoginVO;
import com.starshop.properties.JwtProperties;
import com.starshop.result.Result;
import com.starshop.service.UserService;
import jakarta.annotation.Resource;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) throws Exception {
        //1.查询用户
        User user = lambdaQuery().eq(User::getUsername, userLoginDTO.getUsername()).one();
        //2.如果不存在，抛出异常
        if (user == null) {
            throw new Exception(MessageConstant.USER_NOT_EXISTS);
        }
        //3.存在，查询数据库校验是否正确
        if (!BCrypt.checkpw(userLoginDTO.getPassword(),user.getPassword())) {
            throw new Exception(MessageConstant.PASSWORD_ERROR);
        }
        //4.信息正确则生成token
        Long userId = user.getId();
        HashMap<String, Object> map = new HashMap<>();
        map.put(JwtClaimsConstant.USER_NAME,user.getUsername());
        map.put(JwtClaimsConstant.USER_ID, userId);
        long userTtl = jwtProperties.getUserTtl();
        String token = JwtUtils.createJWT(jwtProperties.getUserSecretKey(), userTtl, map);

        //5.将生成的token存入redis
        String key = RedisKeyConstant.REDIS_TOKEN_PREFIX + userId;

        stringRedisTemplate.opsForValue().set(key,token,userTtl, TimeUnit.MILLISECONDS);

        //6.组装VO返回结果
        return UserLoginVO.builder()
               .id(user.getId())
               .token(token)
               .build();
    }

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
