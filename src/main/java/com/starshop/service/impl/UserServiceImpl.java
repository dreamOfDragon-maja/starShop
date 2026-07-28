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
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";
    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    // 由配置类注入
    @Setter
    private static RedisTemplate<String, Object> redisTemplate;

    /**
     * 刷新token
     * @param refreshToken
     * @return
     */
    @Override
    public Result refreshToken(String refreshToken) {
        //1.从redis中获取refreshToken的信息
        String key = RedisKeyConstant.PREFIX_LOGIN + RedisKeyConstant.REFRESH + RedisKeyConstant.TOKEN +refreshToken;
        Map<String, Object> refreshTokenMap = opsForHash().entries(key);
        //2.判断是否为空，如果为空，返回错误信息
        if(refreshTokenMap.isEmpty()){
            return Result.error(MessageConstant.REFRESH_TOKEN_EXPIRED_ERROR);
        }
        //3.生成新的accessToken
        String accessTokenNew = JwtUtils.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), refreshTokenMap);
        //4.从refreshTokenMap里面获取userId
        Long userId = Long.valueOf(refreshTokenMap.get(JwtClaimsConstant.USER_ID).toString());
        //5，删除旧的refreshToken
        stringRedisTemplate.delete(key);
        //6.生成新的的refreshToken
        User user = User.builder().id(userId).build();
        String refreshTokenNew = getRefreshToken(user);
        //7.封装信息组装
        HashMap<String, Object> resultMap = new HashMap<>(2);
        resultMap.put(ACCESS_TOKEN,accessTokenNew);
        resultMap.put(REFRESH_TOKEN,refreshTokenNew);
        return Result.success(resultMap);
    }



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
        //4.信息正确则生成accessToken
        Long userId = user.getId();
        HashMap<String, Object> map = new HashMap<>();
        map.put(JwtClaimsConstant.USER_NAME,user.getUsername());
        map.put(JwtClaimsConstant.USER_ID, userId);
        long userTtl = jwtProperties.getUserTtl();
        String accessToken = JwtUtils.createJWT(jwtProperties.getUserSecretKey(), userTtl, map);

        //5.生成refreshToken
        String refreshToken = getRefreshToken(user);

        //6.将生成的token存入redis
        String key = RedisKeyConstant.TOKEN + userId;

        stringRedisTemplate.opsForValue().set(key,accessToken,userTtl, TimeUnit.MILLISECONDS);

        //7.组装VO返回结果
        return UserLoginVO.builder()
                .id(user.getId())
                .token(accessToken)
                .refreshToken(refreshToken)
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

    /**
     * 获取 Redis Hash 操作对象
     * @return
     */
    public static HashOperations<String, String, Object> opsForHash() {
        return redisTemplate.opsForHash();
    }
    //刷新 token 格式: UUID
    private String getRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();
        String key = RedisKeyConstant.PREFIX_LOGIN + RedisKeyConstant.REFRESH + RedisKeyConstant.TOKEN +refreshToken;
        HashMap<String, Object> map = new HashMap<>(1);
        map.put(JwtClaimsConstant.USER_ID, String.valueOf(user.getId()));
        stringRedisTemplate.opsForHash().putAll(key, map);
        stringRedisTemplate.expire(key, jwtProperties.getLoginRefreshTokenTtl(), TimeUnit.DAYS);
        return refreshToken;
    }
}
