package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.annotation.Validate;
import com.starshop.common.creation.NicknameCreation;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.result.LoginInfo;
import com.starshop.common.utils.JwtUtils;
import com.starshop.constant.JwtClaimsConstant;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.context.BaseContext;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.mapper.UserMapper;
import com.starshop.pojo.dto.UserLoginDTO;
import com.starshop.pojo.emums.CommonStatus;
import com.starshop.pojo.entity.User;
import com.starshop.pojo.vo.UserVO;
import com.starshop.properties.JwtProperties;
import com.starshop.result.Result;
import com.starshop.service.UserService;
import jakarta.annotation.Resource;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private CopyMapper copyMapper;


    /**
     * 获取当前用户信息
     * @return
     */
    @Override
    public Result<UserVO> getUser() {
        //1.获取当前用户id
        Long userId = BaseContext.getCurrentId();
        //2.根据id查询数据库
        User user = lambdaQuery().eq(User::getId, userId).one();
        //3.判断是否存在
        if(Objects.isNull(user)){
            return Result.error(MessageConstant.USER_NOT_EXISTS);
        }
        //4.利用mapstruct自动映射
        UserVO userVO = copyMapper.usertoUserVO(user);
        return Result.success(userVO);
    }

    /**
     * 刷新token
     * @param refreshToken
     * @return
     */
    @Override
    public Result refreshToken(String refreshToken) {
        //1.从redis中获取refreshToken的信息
        String key = RedisKeyConstant.PREFIX_LOGIN + RedisKeyConstant.REFRESH + RedisKeyConstant.TOKEN +refreshToken;
        Map<String, Object> refreshTokenMap = RedisConnector.opsForHash().entries(key);
        //2.判断是否为空，如果为空，返回错误信息
        if(refreshTokenMap.isEmpty()){
            return Result.error(MessageConstant.REFRESH_TOKEN_EXPIRED_ERROR);
        }
        //3.生成新的accessToken
        String accessTokenNew = JwtUtils.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), refreshTokenMap);
        //4.从refreshTokenMap里面获取userId
        Long userId = Long.valueOf(refreshTokenMap.get(JwtClaimsConstant.USER_ID).toString());
        //5，删除旧的refreshToken
        RedisConnector.delete(key);
        //6.生成新的的refreshToken
        UserVO userVO = UserVO.builder().id(String.valueOf(userId)).build();
        String refreshTokenNew = getRefreshToken(userVO);
        //7.封装信息组装
        HashMap<String, Object> resultMap = new HashMap<>(2);
        resultMap.put(RedisKeyConstant.FIELD_ACCESS_TOKEN,accessTokenNew);
        resultMap.put(RedisKeyConstant.FIELD_REFRESH_TOKEN,refreshTokenNew);
        return Result.success(resultMap);
    }



    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public Result<Object> login(UserLoginDTO userLoginDTO) throws Exception {
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
        UserVO userVO = copyMapper.usertoUserVO(user);
        String userId = userVO.getId();
        HashMap<String, Object> map = new HashMap<>();
        map.put(JwtClaimsConstant.USER_ID, userId);
        long userTtl = jwtProperties.getUserTtl();
        String accessToken = JwtUtils.createJWT(jwtProperties.getUserSecretKey(), userTtl, map);

        //5.生成refreshToken
        String refreshToken = getRefreshToken(userVO);

        //6.将生成的token存入redis
        setUserInfoToRedis(user,userVO,accessToken,refreshToken);

        //7.组装VO返回结果
        return Result.success(new LoginInfo(accessToken,refreshToken,userVO));
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
     * 刷新 token 格式: UUID
     * @param userVO
     * @return
     */
    private String getRefreshToken(UserVO userVO) {
        String refreshToken = UUID.randomUUID().toString();
        String key = RedisKeyConstant.PREFIX_LOGIN + RedisKeyConstant.REFRESH + RedisKeyConstant.TOKEN +refreshToken;
        HashMap<String, Object> map = new HashMap<>(1);
        map.put(JwtClaimsConstant.USER_ID, userVO.getId());
        RedisConnector.opsForHash().putAll(key, map);
        RedisConnector.expire(key, jwtProperties.getLoginRefreshTokenTtl(), TimeUnit.DAYS);
        return refreshToken;
    }

    /**
     * 设置用户信息存入redis
     * @param user
     * @param userVO
     * @param accessToken
     * @param refreshToken
     */
    public void setUserInfoToRedis(User user, UserVO userVO,String accessToken, String refreshToken) {
        if (Objects.isNull(user)) {
            return;
        }
        String key = RedisKeyConstant.PREFIX_LOGIN+RedisKeyConstant.USER_ID+user.getId();
        HashMap<String, Object> loginUserMap = new HashMap<>(4);
        loginUserMap.put(User.Fields.userVO, userVO);
        loginUserMap.put(User.Fields.isEnable, CommonStatus.ACTIVE.getNumber());
        loginUserMap.put(RedisKeyConstant.FIELD_ACCESS_TOKEN, accessToken);
        loginUserMap.put(RedisKeyConstant.FIELD_REFRESH_TOKEN, refreshToken);
        RedisConnector.opsForHash().putAll(key, loginUserMap);
        RedisConnector.expire(key, jwtProperties.getLoginUserInfoInRedisTtl(), TimeUnit.DAYS);
    }
}
